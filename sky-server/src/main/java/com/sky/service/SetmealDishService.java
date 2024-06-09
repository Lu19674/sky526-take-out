package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;

public interface SetmealDishService {
    void save(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO);
}
