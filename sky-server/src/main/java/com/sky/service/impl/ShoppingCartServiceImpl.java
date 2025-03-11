package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author raoxin
 */
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;

    @Transactional
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long currentId = BaseContext.getCurrentId();
        //1.如果都为空，抛出异常
        if(shoppingCartDTO.getDishId() == null && shoppingCartDTO.getSetmealId() == null){
            throw new RuntimeException("菜品id与套餐id不可同时为空");
        }

//        //2.查询购物车中是否存在该菜品或套餐
//        //这里应该根据条件查询所有的，而不是根据id查询，例如不同口味的同样菜品就是两个，所以需要根据条件查询
//            if(shoppingCartDTO.getDishId() != null){
//                existItems =   shoppingCartMapper.selectByDishId(shoppingCartDTO.getDishId());
//            }else{
//                existItems =   shoppingCartMapper.selectBySetmealId(shoppingCartDTO.getSetmealId());
//            }
        ShoppingCart conditions = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,conditions);
        conditions.setUserId(currentId);
        //为何要根据条件进行搜索，因为我们需要判断一个用户的同一个菜的同一个口味的菜品是否存在
        List<ShoppingCart> shoppingCartList  = shoppingCartMapper.selectByConditions(conditions);

        ShoppingCart existItems = null;

        if(shoppingCartList != null && shoppingCartList.size() == 1){
            existItems = shoppingCartList.get(0);
            //如果存在就将数量增加1
            existItems.setNumber(existItems.getNumber() + 1);
            shoppingCartMapper.updateByShoppingCartConditions(existItems);
        }else{
            //不存在的话根据是菜还是套餐选取然后插入
            if (shoppingCartDTO.getDishId() != null){
                Dish dish =  dishMapper.selectById(shoppingCartDTO.getDishId());
                existItems = ShoppingCart.builder()
                        .dishId(shoppingCartDTO.getDishId())
                        .name(dish.getName())
                        .image(dish.getImage())
                        .amount(dish.getPrice())
                        .build();
            }else{
                Setmeal setMeal =  setMealMapper.selectById(shoppingCartDTO.getSetmealId());
                existItems = ShoppingCart.builder()
                        .setmealId(shoppingCartDTO.getSetmealId())
                        .name(setMeal.getName())
                        .image(setMeal.getImage())
                        .amount(setMeal.getPrice())
                        .build();
            }
            //数量时间自己设置，口味是传过来的，其它字段根据查询得出
            existItems.setNumber(1);
            existItems.setUserId(currentId);
            existItems.setCreateTime(LocalDateTime.now());
            existItems.setDishFlavor(shoppingCartDTO.getDishFlavor());
            shoppingCartMapper.insert(existItems);
        }
    }

    @Override
    public List<ShoppingCart> list() {
        Long currentId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByUserId(currentId);
        return shoppingCartList;
    }

    @Override
    public void cleanshoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long currentId = BaseContext.getCurrentId();
        //1.因为是删除，所以商品必然存在，但是我们还是验证一下，如果不存在就抛出异常
        //2.查询，数量是1就删除
        //3.数量大于1就减1
        ShoppingCart conditions = ShoppingCart.builder()//赋值属性，例如dishID，或者 setmealid 还有口味 id和口味定位到商品
                                                .id(currentId)
                                                .setmealId(shoppingCartDTO.getSetmealId())
                                                .dishId(shoppingCartDTO.getDishId())
                                                .dishFlavor(shoppingCartDTO.getDishFlavor())
                                                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectByConditions(conditions);
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new RuntimeException("购物车中没有该商品");
        }
        if(shoppingCartList.size() == 1){
            ShoppingCart shoppingCart = shoppingCartList.get(0);
            Integer number = shoppingCart.getNumber();
            if(number == 1){
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else{
                shoppingCart.setNumber(number - 1);
                shoppingCartMapper.updateByShoppingCartConditions(shoppingCart);
            }
        }
    }
}
