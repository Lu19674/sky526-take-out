package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 修改购物车对应商品数量
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id=#{id}")
    void updateNumberById(ShoppingCart cart);

    /**
     * 新增购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime}) ")
    void insert(ShoppingCart shoppingCart);

    /**
     * 批量新增购物车数据
     * @param cartList
     */
    void insertBatch(List<ShoppingCart> cartList);

    /**
     * 清空购物车
     */
    @Delete("delete from shopping_cart where user_id=#{userId}")
    void clearByUserId(Long userId);

    /**
     * 删除购物车一个商品
     * @param cart
     */
    @Delete("delete from shopping_cart where id=#{id}")
    void removeOneById(ShoppingCart cart);
}
