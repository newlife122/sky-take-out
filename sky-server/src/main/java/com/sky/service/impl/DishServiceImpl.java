package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author raoxin
 */
@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    DishMapper dishMapper;

    @Autowired
    DishFlavorMapper dishFlavorMapper;

    @Autowired
    SetMealDishMapper setMealDishMapper;

    /**
     * 保存菜品
     * 同时保存对应口味
     * 注意开启事务
     * @param dishDTO
     */
    @Transactional
    @Override
    public void save(DishDTO dishDTO) {

        //0.首先构建对象
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dish.setStatus(1);

        //0.菜品名称必须唯一，所以要先根据名字查询
        Dish dish1 =  dishMapper.getByName(dishDTO.getName());
        if(dish1 != null){
            log.error("菜品名称重复");
            throw new RuntimeException("菜品名称重复");
        }

        //1.插入以后主键值回调
        dishMapper.insert(dish);
        if (dish.getId() == null){
            log.error("保存菜品失败");
            throw new RuntimeException("保存菜品失败");
        }

        //2.保存对应的口味，这里保存口味需要用到保存的id
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() != 0){
            for(DishFlavor flavor:flavors){
                //遍历取出，然后赋值id
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }



    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        if(page == null){
            throw new RuntimeException("查询失败");
        }
        PageResult pageResult =PageResult.builder()
                .total(page.getTotal())
                .records(page.getResult())
                .build();
        return pageResult;
    }

    // TODO 在完成禁售和套餐的功能后，还要测试
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //0.查询出所有菜品，如果有在售的抛出异常
        List<Dish> dishs = dishMapper.selectByIdList(ids);
        if (dishs == null || dishs.size() == 0){
            return;
        }

        for (Dish dish : dishs) {
            if (dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException("菜品正在售卖中，不能删除");
            }
        }

        //1.查询套餐，如果有关联的套餐，抛出异常
        List<Dish> dishs1 =  setMealDishMapper.selectByDishIds(ids);
        if (dishs1 != null && dishs1.size() != 0){
            throw new DeletionNotAllowedException("有关联套餐，不能删除");
        }

        //2.删除菜品
        dishMapper.deleteBatch(ids);
        //3.删除对应的口味
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     *
     * @param dishDTO
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        //1.修改菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //通过id
        dishMapper.update(dish);

        //2.修改口味
        //2.1先删除原来的口味
        //2.2再添加新的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        if (dishDTO.getFlavors() != null && dishDTO.getFlavors().size() != 0){
            for (DishFlavor flavor : dishDTO.getFlavors()) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(dishDTO.getFlavors());
        }
    }

    @Override
    public DishVO queryById(Long id) {
        DishVO dishVo = new DishVO();

        //1.查询菜品
        Dish dish = dishMapper.selectById(id);

        if (dish == null){
            throw new RuntimeException("菜品不存在");
        }

        BeanUtils.copyProperties(dish,dishVo);

        //2.查询口味
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);
        if (flavors != null){
            dishVo.setFlavors(flavors);
        }

        return dishVo;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> queryByCotegoryId(Long categoryId) {
        List<Dish> dishes = dishMapper.selectByCotegoryId(categoryId);
        return dishes;
    }

    @Override
    public List<DishVO> queryEnableDishVoByCotegoryId(Long categoryId) {
        List<DishVO>  dishVOList = dishMapper.selectEnableDishVoByCotegoryId(categoryId);
        return dishVOList;
    }

}
