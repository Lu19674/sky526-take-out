package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜品业务逻辑
 */
@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品及相关口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        //属性拷贝
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setStatus(0);
        //1.向dish菜品表插入一条数据
        dishMapper.insert(dish);
        Long dishId = dish.getId();//主键返回（获取insert语句生成的id）

        //2.向口味表批量插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        flavors.forEach(flavor ->{
            flavor.setDishId(dishId);
        });//遍历为每一个口味对象附上菜品的主键
        dishFlavorMapper.insertBatch(flavors);
    }

    /**
     * 条件分页查询菜品
     * @param dto
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dto);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
