package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.models.Cart;

import java.math.BigDecimal;

public interface ICartService {

    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotal(Long id);

    Long generateNewCartId();
}
