package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品和套餐的关联表操作接口
 */
@Mapper
public interface SetmealDishMapper {


    List<Long> ifNullByDishIds(List<Long> ids);
}
