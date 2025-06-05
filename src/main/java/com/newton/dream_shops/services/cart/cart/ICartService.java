package com.newton.dream_shops.services.cart.cart;

import com.newton.dream_shops.models.cart.Cart;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

public interface ICartService {

    Cart getCartByUserId(Long userId);

    Cart getOrCreateCartForUser(Long userId);

    Cart getOrCreateCartForCurrentUser(HttpServletRequest request);

    void clearCartForUser(Long userId);

    void clearCartForCurrentUser(HttpServletRequest request);

    BigDecimal getTotalForUser(Long userId);

    BigDecimal getTotalForCurrentUser(HttpServletRequest request);
}
