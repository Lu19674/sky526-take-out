package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dto);

    void deleteBacth(List<Long> ids);

    DishVO getByid(Long id);

    void updateWithflavor(DishDTO dishDTO);

    List<DishVO> getByCategoryId(Long id,String name);
}
