package com.tcl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tcl.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
