package com.tcl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.reggie.common.CustomException;
import com.tcl.reggie.dto.SetmealDto;
import com.tcl.reggie.entity.Setmeal;
import com.tcl.reggie.entity.SetmealDish;
import com.tcl.reggie.mapper.SetmealMapper;
import com.tcl.reggie.service.SetmealDishService;
import com.tcl.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>implements SetmealService {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 保存套餐
        this.save(setmealDto);

        // 获取套餐setMealId
        Long setMealId = setmealDto.getId();

        // 获取套餐里菜品种类
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        // 为菜品种类封装套餐id
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());

        // 保存菜品种类
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id查询套餐信息和菜品信息
     * @param id
     * @return DishDto
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 1.查询套餐基本信息
        Setmeal setmeal = this.getById(id);
        // 2.查询套餐关联的菜品信息
        SetmealDto setmealDto = new SetmealDto();
        // 2.1拷贝到dto
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 2.1查询关联菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        // 给dto设置关联菜品的值
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        // 保存修改的setmal
        this.updateById(setmealDto);

        // 更新setmeal_dish表
        // 先删除旧的更新setmeal_dish表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        // 添加新的数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        // 为菜品种类封装套餐id
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        // 保存菜品种类
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐和套餐的菜品
     * @param ids 套餐id
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        // 1.查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(queryWrapper);
        if(count>0) {
            // 如果不能删除，抛出业务异常
            throw new CustomException("套餐正在起售中，不能删除");
        }
        // 2.如果可以删除，先删除套餐表数据
        this.removeByIds(ids);
        // 3.删除关联菜品数据
        // 3.1找到关联菜品的id
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        // 3.2删除setmel_dish中的菜品
        setmealDishService.remove(dishLambdaQueryWrapper);
    }
}
