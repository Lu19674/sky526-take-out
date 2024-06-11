package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
