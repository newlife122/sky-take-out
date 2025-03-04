package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.vo.CategoryPageQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author raoxin
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "C端-分类相关接口")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("list")
    @ApiOperation(value = "通过type分类查询")
    public Result<List<Category>> list(Integer type) {
        log.info("分类查询");
        Category category = Category.builder()
                            .type(type)
                            .status(StatusConstant.ENABLE)
                            .build();
        List<Category> list = categoryService.selectByConditions(category);

        return Result.success(list);
    }
}
