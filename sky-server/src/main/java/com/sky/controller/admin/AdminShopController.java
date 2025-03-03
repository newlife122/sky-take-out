package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author raoxin
 */
@RestController
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺操作接口")
public class AdminShopController {
    public static final String SHOP_STATUS_KEY = "sky:shop:status";
    @Autowired
    RedisTemplate redisTemplate;
    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    public Result getStatus() {
        Integer status =(Integer)redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        log.info("设置店铺状态:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }

    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置店铺状态:{}", status == 1 ? "设置为营业" : "设置为打烊中");
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);
        return Result.success();
    }
}
