package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFIll;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author rxxxx
* @description 针对表【category(菜品及套餐分类)】的数据库操作Mapper
* @createDate 2025-02-27 20:35:23
* @Entity generator.Category
*/
@Mapper
public interface CategoryMapper {
    List<Category> getByType(Integer type);

    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    @AutoFIll(OperationType.UPDATE)
    void upadate(Category category);

    @AutoFIll(OperationType.INSERT)
    void insert(Category category);

    void deleteById(Long id);

    Category getById(Long id);

    List<Category> selectByConditions(Category category);
}
