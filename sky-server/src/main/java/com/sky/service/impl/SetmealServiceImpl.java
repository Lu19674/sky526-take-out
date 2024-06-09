package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);
        //插入套餐表
        setmealMapper.insert(setmeal);
        Long setmealId = setmeal.getId();//主键返回

        //插入套餐菜品关联表
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && setmealDishes.size() > 0) {
            setmealDishes.forEach(sd -> {
                sd.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 分页查询套餐
     *
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(pageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 毗连删除套餐
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断是否有起售中的套餐
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getByid(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //执行套餐批量删除
        setmealMapper.deleteBatch(ids);

        //执行泰餐与菜品的关联表的相关批量删除
        setmealDishMapper.deleteBatch(ids);


    }

    /**
     * 根据id查询套餐及套餐菜品关联表
     *
     * @param id
     * @return
     */
    @Transactional
    public SetmealVO getById(Long id) {
        //查询套餐基本信息
        Setmeal setmeal = setmealMapper.getByid(id);

        //查询关联的套餐菜品关联表信息
        List<SetmealDish> setmealDishes = setmealDishMapper.queryBySetmealId(id);
        //查询对应的分类名称
        Category category = categoryMapper.getById(setmeal.getCategoryId());
        String categoryName = category.getName();

        //封装进VO数据返回
        SetmealVO setmealVO = SetmealVO.builder()
                .categoryName(categoryName)
                .setmealDishes(setmealDishes)
                .build();
        BeanUtils.copyProperties(setmeal, setmealVO);
        return setmealVO;

    }

    /**
     * 修改套餐及套餐菜品关联表信息
     *
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        Long setmealId = setmealDTO.getId();//套餐id

        //删除套餐菜品关联信息
        setmealDishMapper.deleteBatch(Collections.singletonList(setmealId));

        //插入新套餐菜品关联信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        setmealDishes.forEach(sd -> {
            sd.setSetmealId(setmealId);
        });//遍历每一个套餐菜品关联对象为其每一个 setmeal_id 赋上套餐主键id
        setmealDishMapper.insertBatch(setmealDishes);

    }
}
