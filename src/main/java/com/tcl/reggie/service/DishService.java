package com.tcl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.reggie.dto.DishDto;
import com.tcl.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
