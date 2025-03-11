package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品管理
 */
@Mapper
public interface UserMapper {

    User getUserByOpenId(String openId);

    void insert(User user);

    User selectById(Long currentId);

    Integer getUserCntByPeriod(LocalDateTime begin, LocalDateTime end);
}
