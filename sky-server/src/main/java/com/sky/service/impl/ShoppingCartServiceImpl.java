package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //先判断此次添加的商品在次用户的购物车当中是否已经存在了
        ShoppingCart shoppingCart =new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());//设置用户id区别不同用户的购物车（从本线程变量中取得）
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);//执行查询

        //存在：只需修改 数量+1
        if(list!=null && list.size()>0){
            //根据条件查询只能查出一条，所以取list中第一个元素即可
            ShoppingCart cart = list.get(0);
            //在原来数量上 +1
            cart.setNumber(cart.getNumber()+1);
            //执行修改
            shoppingCartMapper.updateNumberById(cart);
        }

        //不存在：添加进该用户对应的购物车
        else {
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            //如果本次添加的是 菜品 商品：
            if(dishId !=null){
                Dish dish =new Dish();
                dish =dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
            //如果本次添加的是 套餐 商品
            else if(setmealId != null){
                Setmeal setmeal =new Setmeal();
                setmeal= setmealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }
            //无论是套餐还是菜品 都统一设置的属性 ：数量（1） 创建时间（now（））
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        //设置购物车的用户id
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart);
        return cartList;
    }

    /**
     * 清空购物车
     */
    @Override
    public void emptyCart() {
        Long userId=BaseContext.getCurrentId();
        shoppingCartMapper.clearByUserId(userId);
    }
}
