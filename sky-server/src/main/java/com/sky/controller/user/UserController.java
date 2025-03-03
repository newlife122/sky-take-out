package com.sky.controller.user;

import com.sky.dto.UserLoginDTO;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author raoxin
 */
@RestController
@RequestMapping("/user/user")
@Slf4j
@Api(tags = "C端-用户接口")
public class UserController {
    @Autowired
    UserService userService;
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO code) {
        UserLoginVO userLoginVO = userService.login(code);
        log.info("用户登录成功：{}",userLoginVO);
        return Result.success(userLoginVO);
    }
}
