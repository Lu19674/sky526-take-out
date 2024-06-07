package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
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
    @Autowired
    private SetmealDishMapper setmealDishMapper;

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
        if (flavors !=null && flavors.size() >0) {
            flavors.forEach(flavor ->{
                flavor.setDishId(dishId);
            });//遍历为每一个口味对象附上菜品的主键
            dishFlavorMapper.insertBatch(flavors);
        }
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

    /**
     * 根据ids批量删除菜品及其关联的口味
     * @param ids
     */
    @Transactional
    public void deleteBacth(List<Long> ids) {
        //1判断当前菜品是否能够删除--是否存在起售中的状态的菜品？
        for (Long id : ids) {
            Dish dish= dishMapper.queryById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2判断但钱菜品是否是能够删除--是否被套餐（setmeal）关联了？
        List<Long> list = setmealDishMapper.ifNullByDishIds(ids);
        if(list !=null && list.size() > 0){
            throw  new DeletionNotAllowedException((MessageConstant.DISH_BE_RELATED_BY_SETMEAL));
        }
        //3删除菜品中的菜品数据
        dishMapper.deleteBaceh(ids);
        //4删除菜品关联的口味（dish_flavor）数据
        dishFlavorMapper.deleteBaceh(ids);
    }
}
