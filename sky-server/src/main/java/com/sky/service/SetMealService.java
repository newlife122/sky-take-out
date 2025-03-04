package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * @author raoxin
 */
public interface SetMealService {
    void saveSetMeal(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void updateSetMeal(SetmealDTO setmealDTO);

    SetmealVO getById(Long id);

    void startOrStop(Integer status, Long id);

    void deleteBatch(List<Long> ids);

    List<Setmeal> listByCategoryId(Long categoryId);


    List<DishItemVO> queryDishBySetMealId(Long id);


    List<Setmeal> QueryBySetmealConditions(Setmeal setmeal);
}
