package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author raoxin
 */
@RestController("userSetMealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端-套餐管理相关接口")
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @GetMapping("list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(@RequestParam (required = true) Long categoryId) {
        Setmeal setmeal = Setmeal.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        List<Setmeal> setmealList = setMealService.QueryBySetmealConditions(setmeal);
        return Result.success(setmealList);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询菜品的详情")
    public Result<List<DishItemVO>> queryByCategoryId(@PathVariable Long id) {
        List<DishItemVO>  dishItemVOList = setMealService.queryDishBySetMealId(id);
        return Result.success(dishItemVOList);
    }
}
