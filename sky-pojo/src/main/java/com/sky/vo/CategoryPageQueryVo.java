package com.sky.vo;

import com.sky.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author raoxin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPageQueryVo implements Serializable {
    private Long total;
    private List<Category> records;
}
