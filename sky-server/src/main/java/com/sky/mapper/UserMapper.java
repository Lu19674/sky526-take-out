package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据 openid 查询微信用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    public User getByOpenId(String openid);

    /**
     * 新增用户
     * @param newUser
     */
    void insert(User newUser);

    /**
     * 根据用户id查询用户
     * @param id
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 根据时间段查询用户数量
     * @param map
     * @return
     */
    Integer sumByMap(Map<String, Object> map);
}
