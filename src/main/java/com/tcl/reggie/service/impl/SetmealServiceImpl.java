package com.tcl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tcl.reggie.entity.Setmeal;
import com.tcl.reggie.mapper.SetmealMapper;
import com.tcl.reggie.service.SetmealService;
import org.springframework.stereotype.Service;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>implements SetmealService {
}
