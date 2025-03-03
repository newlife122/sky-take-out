package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

/**
 * @author raoxin
 */
public interface UserService {
    UserLoginVO login(UserLoginDTO userLoginDTO);

    String getOpenId(String code);
}
