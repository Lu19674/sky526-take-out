package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品和套餐的关联表操作接口
 */
@Mapper
public interface SetmealDishMapper {

    /**
     * 判断相关菜品是否有关联的套餐
     * @param dishIds
     * @return
     */
    List<Long> ifNullByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐菜品关联信息
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
