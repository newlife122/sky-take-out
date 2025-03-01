package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface SetMealMapper {

    @AutoFIll(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> selectAll(SetmealPageQueryDTO setmealPageQueryDTO);

    Page<SetmealVO> selectByCondition(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO selectById(Long id);


    Setmeal selectByName(String name);

    @AutoFIll(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    void deleteBatch(List<Long> ids);
}
