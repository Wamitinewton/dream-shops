package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.models.cart.CartItem;

public interface ICartItemService {
    void addItemToCart(Long cartId, Long productId, int quantity);
    void removeItemFromCart(Long cartId, Long productId);
    void updateItemInCart(Long cartId, Long productId, int quantity);

    CartItem getCartItemById(Long cartId, Long productId);
}
