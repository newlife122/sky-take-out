package com.sky.controller.admin;

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
@RestController("adminCategoryController")
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("list")
    @ApiOperation(value = "通过type分类查询")
    public Result<List<Category>> list(Integer type) {
        log.info("分类查询");
        List<Category> list = categoryService.selectByType(type);
        return Result.success(list);
    }

    @GetMapping("page")
    @ApiOperation(value = "分类分页查询")
    public Result<CategoryPageQueryVo> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分类分页查询");
        CategoryPageQueryVo categoryPageQueryVo = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(categoryPageQueryVo);
    }

    @PutMapping
    @ApiOperation(value = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类");
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "修改分类状态")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        log.info("修改分类状态");
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result saveCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类");
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation(value = "删除分类")
    public Result deleteCategoryById(Long id) {
        log.info("删除分类");
        categoryService.deleteCategoryById(id);
        return Result.success();
    }
}
