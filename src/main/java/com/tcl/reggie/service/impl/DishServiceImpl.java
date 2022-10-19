package com.tcl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.reggie.dto.DishDto;
import com.tcl.reggie.entity.Dish;
import com.tcl.reggie.entity.DishFlavor;
import com.tcl.reggie.mapper.DishMapper;
import com.tcl.reggie.service.DishFlavorService;
import com.tcl.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存风味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品
        this.save(dishDto);

        // 获取菜品dishID
        Long dishId = dishDto.getId();

        // 获取菜品风味
        List<DishFlavor> flavors = dishDto.getFlavors();

        // 为flavor中的每个item封装dishID
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        // 保存口味表
        dishFlavorService.saveBatch(flavors);

    }


    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return DishDto
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 查询菜品基本信息
        Dish dish = this.getById(id);

        // 查询菜品关联风味信息,并保存到DishDto
        DishDto dishDto = new DishDto();

        // 拷贝dish到dishDto
        BeanUtils.copyProperties(dish,dishDto);

        // 查询关联风味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        // 设置值
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表
        this.updateById(dishDto);

        // 更新口味表
        // 1.先删除旧的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        // 2.添加新的数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        // 2.1注意保存口味的dishId
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存口味表
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    @Transactional
    public void deleteWithFlavor(Long ids) {
        // 删除菜品
        this.removeById(ids);

        // 删除关联的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);
    }
}
