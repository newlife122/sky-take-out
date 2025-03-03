package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author raoxin
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    WeChatProperties weChatProperties;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserMapper userMapper;


    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        //1.获取openid，如果没有获取到会抛出异常
        //2.根据openid查询数据库，如果存在，则直接登录，如果不存在，则要插入这个User
        //3.根据id生成token，返回给前端
        String openId = getOpenId(userLoginDTO.getCode());
        if (openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.getUserByOpenId(openId);

        //没有就创建
        if(user == null){
            user = User.builder()
                    .openid(openId)
                    .build();
            userMapper.insert(user);
        }

        Map<String,Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.USER_ID,user.getId());
        JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),map);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(openId)
                .token(JwtUtil.createJWT(jwtProperties.getUserSecretKey(),jwtProperties.getUserTtl(),map))
                .build();
        return userLoginVO;
    }

    @Override
    public String getOpenId(String code){
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        //使用Get来发送请求
        String s = HttpClientUtil.doGet(weChatProperties.getUrlForOpenid(), map);
        JSONObject jsonObject = JSONObject.parseObject(s);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
