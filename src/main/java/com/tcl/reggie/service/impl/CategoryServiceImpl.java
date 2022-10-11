package com.tcl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.reggie.common.CustomException;
import com.tcl.reggie.entity.Category;
import com.tcl.reggie.entity.Dish;
import com.tcl.reggie.entity.Setmeal;
import com.tcl.reggie.entity.SetmealDish;
import com.tcl.reggie.mapper.CategoryMapper;
import com.tcl.reggie.service.CategoryService;
import com.tcl.reggie.service.DishService;
import com.tcl.reggie.service.SetmealDishService;
import com.tcl.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    // 需要注入关联菜品和套餐的Service
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;
    /**
     * 根据ID删除分类, 删除之前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 查询是否关联了菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 条件查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        if(dishCount>0){
            // 已经关联菜品,抛出异常
            throw new CustomException("当前分类项关联菜品，不能删除");
        }

        // 查询是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        if(setmealCount>0){
            // 已经关联套餐, 抛出异常
            throw new CustomException("当前分类项关联套餐，不能删除");
        }

        // 正常删除分类
        super.removeById(id);
    }
}
