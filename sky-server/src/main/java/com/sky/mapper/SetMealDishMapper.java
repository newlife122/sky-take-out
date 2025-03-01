package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    void deleteBatch(List<Long> ids);

    List<Dish> selectByDishIds(List<Long> ids);

    void insertSetMealDish(SetmealDish setmealDish);

    void insertSetMealDishBatch(List<SetmealDish> setmealDishes);

    List<SetmealDish> selectByDishId(Long id);

    void deleteBySetMealId(Long id);
}
