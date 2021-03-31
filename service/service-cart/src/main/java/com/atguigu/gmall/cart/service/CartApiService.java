package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartApiService {
    void addCart(CartInfo cartInfo);

    List<CartInfo> getCartList(String userId);

    void checkCart(CartInfo cartInfo);
}
