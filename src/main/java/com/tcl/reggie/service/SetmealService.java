package com.tcl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.reggie.dto.SetmealDto;
import com.tcl.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithSetmealDish(SetmealDto setmealDto);

    SetmealDto getByIdWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);
}
