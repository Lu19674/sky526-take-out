package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController(value = "userShopController")
@Api(tags="店铺相关接口")
@RequestMapping("/user/shop")
public class ShopController {

    public static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 查询店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺营业状态")
    public Result<Integer> getStatus(){
        //从Redis中取出 key为 SHOP_STATUS 的 值status
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("当前店铺营业状态为{}",status==1?"营业中":"打样中");
        return Result.success(status);
    }
}
