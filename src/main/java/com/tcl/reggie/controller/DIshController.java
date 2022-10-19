package com.tcl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcl.reggie.common.R;
import com.tcl.reggie.dto.DishDto;
import com.tcl.reggie.entity.Category;
import com.tcl.reggie.entity.Dish;
import com.tcl.reggie.entity.DishFlavor;
import com.tcl.reggie.entity.Employee;
import com.tcl.reggie.service.CategoryService;
import com.tcl.reggie.service.DishFlavorService;
import com.tcl.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DIshController {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        // 删除缓存
        String key = "dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);

        return R.success("新增菜品成功！！！");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        // 分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        // 添加搜索条件
        queryWrapper.like(name!=null,Dish::getName, name);

        // 添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        // 执行分页查询
        dishService.page(pageInfo,queryWrapper);

        // 对象拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();

        // 按照category取出id，并取出name赋值到dishDto中的categoryName
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            // 根据Id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id){
        log.info("根据id查询菜品信息和对应的口味信息...");
        // 查两张表，因此需要在Service中扩展方法
        DishDto idWithFlavor = dishService.getByIdWithFlavor(id);
        if(idWithFlavor!=null){
            return R.success(idWithFlavor);
        }
        return R.error("没有查询到对应员工信息");
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

        // 清理所有菜品缓存数据，再添加

        // 精确清理数据
        String key = "dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("修改菜品成功！！！");
    }

//    /**
//     * (套餐菜品)根据条件查询对应的菜品数据
//     * @param dish
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        // 查询起售状态的菜品
//        queryWrapper.eq(Dish::getStatus, 1);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }

    /**
     * (套餐菜品)根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(DishDto dish){
        List<DishDto> dishDtoList = null;
        // 动态拼接key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        // 先从Redis中获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        // 如果存在，直接返回，无需查询数据库
        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());

        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        // 查询起售状态的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId();
            // 根据Id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            // 查询口味数据
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);

            List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());
        // 将数据缓存到Redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
