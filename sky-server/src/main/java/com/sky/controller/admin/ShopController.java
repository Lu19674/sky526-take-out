package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController(value = "adminShopController")
@Api(tags="店铺相关接口")
@RequestMapping("/admin/shop")
public class ShopController {

    public static final String KEY="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为： {}",status==1?"营业中":"打样中");
        //String类型的 SHOP_STATUS ： #{status} 存入Redis
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

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
