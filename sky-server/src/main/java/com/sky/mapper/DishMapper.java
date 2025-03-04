package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品管理
 */
@Mapper
public interface DishMapper {

    @AutoFIll(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    Dish getByName(String name);

    void deleteBatch(List<Long> ids);

    List<Dish> selectByIdList(List<Long> ids);

    @AutoFIll(OperationType.UPDATE)
    void update(Dish dish);

    Dish selectById(Long id);

    List<Dish> selectByCotegoryId(Long categoryId);

    List<Dish> selectBySetMealId(Long id);

    List<DishVO> selectEnableDishVoByCotegoryId(Long categoryId);
}
