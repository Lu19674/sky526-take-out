package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@Slf4j
@RequestMapping("/admin/dish")
@RestController
@Api(tags="菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "DishCache",key = "#dishDTO.categoryId")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品 {}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dto
     * @return
     */
    @ApiOperation("分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dto){
        log.info("分页查询：{}",dto);
        PageResult pageResult= dishService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 根据ids批量删除菜品及其关联的口味
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    @CacheEvict(cacheNames="DishCache",allEntries = true)
    public Result removeByIds(@RequestParam List<Long> ids){
        log.info("批量删除菜品 {}",ids);
        dishService.deleteBacth(ids);
        return Result.success();
    }

    /**
     * 根据id查询回显菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询回显菜品")
    public Result<DishVO> QueryById(@PathVariable Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO = dishService.getByid(id);

        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames="DishCache" ,allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品 {}",dishDTO);
        dishService.updateWithflavor(dishDTO);
        return Result.success();
    }

    /**
     * 根据菜品分类id及菜品name查询菜品
"     * @param categoryId"
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据菜品分类id及菜品name查询菜品")
    public Result<List<DishVO>> queryByCategoryId(Long categoryId,@RequestParam(name="name",required = false) String name){
        List<DishVO> dishVOs=dishService.getByCategoryId(categoryId,name);
        return Result.success(dishVOs);
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售菜品")
    @CacheEvict(cacheNames="DishCache" ,allEntries = true)
    public Result updateStatusById(@PathVariable Integer status,Long id){
        log.info("菜品起售或停售：{}->{}",id,status);
        dishService.updateStatusById(status,id);

        return Result.success();
    }

}
