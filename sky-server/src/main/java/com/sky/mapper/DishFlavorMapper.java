package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author raoxin
 */
@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    void deleteBatch(List<Long> ids);

    void deleteByDishId(Long id);

    List<DishFlavor> selectByDishId(Long id);
}
