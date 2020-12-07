package com.atguigu.gulimall.cart.service;

import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;

import java.util.List;

public interface CartService {
    CartItemVo addCartItem(Long skuId, Integer num);

    CartItemVo getCartItem(Long skuId);

    CartVo getCart();

    void checkCart(Long skuId, Integer isChecked);

    void deleteItem(Long skuId);

    void changeItemCount(Long skuId, Integer num);

    List<CartItemVo> getCheckedItems();

}
