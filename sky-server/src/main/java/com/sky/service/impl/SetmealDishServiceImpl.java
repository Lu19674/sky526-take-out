package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealDishService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealDishServiceImpl implements SetmealDishService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        //插入套餐表
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();//主键返回

        //插入套餐菜品关联表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes !=null && setmealDishes.size() > 0) {
            setmealDishes.forEach(sd -> {
                sd.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询套餐
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(),pageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }
}
