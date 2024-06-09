package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SetmealPageQueryDTO implements Serializable {
    //页码
    private int page;

    //每页展示记录数
    private int pageSize;

    //套餐名称
    private String name;

    //分类id
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
