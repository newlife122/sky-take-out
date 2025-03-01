package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.vo.CategoryPageQueryVo;

import java.util.List;

/**
 * @author raoxin
 */
public interface CategoryService {
    List<Category> selectByType(Integer type);

    CategoryPageQueryVo pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void update(CategoryDTO categoryDTO);

    void updateStatus(Integer status, Long id);

    void save(CategoryDTO categoryDTO);

    void deleteCategoryById(Long id);
}
