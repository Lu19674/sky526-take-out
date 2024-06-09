package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealDishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 套餐相关接口
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags="套餐相关接口")
public class SetmealController {


    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}",setmealDTO);
        setmealDishService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询套餐
     * @param pageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询套餐")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO pageQueryDTO){
        log.info("分页查询套餐：{}",pageQueryDTO);
        PageResult pageResult = setmealDishService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

}
