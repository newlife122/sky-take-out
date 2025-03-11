package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.controller.admin.SetMealController;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * @author raoxin
 */
@Service
 public class SetMealServiceImpl implements SetMealService {
    @Autowired
    private SetMealMapper setMealMapper;

    @Autowired
    private SetMealDishMapper setMealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Transactional
    @Override
    public void saveSetMeal(SetmealDTO setmealDTO) {
        //1.插入这个套餐，然后返回id
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmeal.setStatus(StatusConstant.DISABLE);

        setMealMapper.insert(setmeal);
        if(setmeal.getId() == null){
            throw new RuntimeException("保存套餐失败");
        }

        //2.插入套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.size() == 0){
            throw new RuntimeException("保存套餐失败，未上传菜品");
        }
        //2.1需要将套餐id设置到每一个关系表中
        setmealDishes.forEach(
            setmealDish -> {
                setmealDish.setSetmealId(setmeal.getId());
            }
        );
        setMealDishMapper.insertSetMealDishBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Setmeal setmeal = Setmeal.builder()
                .categoryId(setmealPageQueryDTO.getCategoryId())
                .name(setmealPageQueryDTO.getName())
                .status(setmealPageQueryDTO.getStatus())
                .build();
        Page<SetmealVO> page = setMealMapper.selectByCondition(setmeal);
        PageResult pageResult = PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
        return pageResult;
    }

    @Transactional
    @Override
    public void updateSetMeal(SetmealDTO setmealDTO) {
        //1.首先修改setmeal表 直接修改
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setMealMapper.update(setmeal);
        //2.修改setmeal_dish表
        //2.1根据套餐id删除所有的，然后再直接插入就可以了
        setMealDishMapper.deleteBySetMealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(
            setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            }
        );
        setMealDishMapper.insertSetMealDishBatch(setmealDishes);
    }

    @Transactional
    @Override
    public SetmealVO getById(Long id) {
        //1.先根据id查询SetMeal
        Setmeal setmeal = setMealMapper.selectById(id);
        if (setmeal == null){
            throw new RuntimeException("套餐不存在");
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        //2.然后再根据SetMeal的id查询SetMealDish
        List<SetmealDish> setmealDishes = setMealDishMapper.selectByDishId(setmeal.getId());
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Transactional
    @Override
    public void startOrStop(Integer status, Long id) {
        //注意还要是做起售这个动作才进这个判断
        if(status == StatusConstant.ENABLE){
            //1.根据id查询其所有的菜，如果有菜还在停售，这个套餐就无法开启
            List<Dish> dishs = dishMapper.selectBySetMealId(id);
            if(!(dishs == null || dishs.size() == 0)){
                for(Dish dish: dishs){
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                }
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();
        setMealMapper.update(setmeal);
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //每次取一个id
        //找到它的套餐id，然后删除套餐和菜品的关联关系，再删除套餐
        if(ids == null || ids.size() == 0){
            throw new RuntimeException("删除失败,未传递参数");
        }
        //循环取出，然后根据套餐id删除相应的 套餐菜品对应表
        for(Long id:ids){
            setMealDishMapper.deleteBySetMealId(id);
        }
        //一次删除所有的套餐
        setMealMapper.deleteBatch(ids);
    }

    @Override
    public List<Setmeal> listByCategoryId(Long categoryId) {

        List<Setmeal> setmealList = setMealMapper.selectByCotegoryId(categoryId);
        return setmealList;
    }


    @Override
    public List<DishItemVO> queryDishBySetMealId(Long id) {
        List<DishItemVO>  dishItemVOList =  setMealMapper.selectDishItemVOBySetMealId(id);
        return dishItemVOList;
    }

    @Override
    public List<Setmeal> QueryBySetmealConditions(Setmeal setmeal) {
        List<Setmeal> setmealList = setMealMapper.selectBySetmealConditions(setmeal);
        return setmealList;
    }



}
