package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author raoxin
 */
@Mapper
public interface ShoppingCartMapper {
    ShoppingCart selectByDishId(Long dishId);

    ShoppingCart selectBySetmealId(Long setmealId);

    void updateByShoppingCartConditions(ShoppingCart existItems);

    void insert(ShoppingCart existItems);

    List<ShoppingCart> selectByUserId(Long currentId);

    List<ShoppingCart> selectByConditions(ShoppingCart conditions);

    void deleteByUserId(Long currentId);

    void deleteById(Long id);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
