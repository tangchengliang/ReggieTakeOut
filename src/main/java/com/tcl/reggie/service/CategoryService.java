package com.tcl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.reggie.entity.Category;

public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
