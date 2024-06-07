package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     * 批量新增口味
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ids批量删除口味
     * @param dishIds
     */
    void deleteBaceh(List<Long> dishIds);

    /**
     * 根据菜品id查询
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> queryByDishId(Long dishId);
}
