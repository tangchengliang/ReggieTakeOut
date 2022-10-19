package com.tcl.reggie.dto;

import com.tcl.reggie.entity.Setmeal;
import com.tcl.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
