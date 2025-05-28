package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.models.cart.CartItem;

import jakarta.servlet.http.HttpServletRequest;

public interface ICartItemService {
    void addItemToCart(Long userId, Long productId, int quantity);
 
   void removeItemFromCart(Long userId, Long productId);
   
   void updateItemInCart(Long userId, Long productId, int quantity);

   CartItem getCartItemById(Long userId, Long productId);

    void addItemToCartForUser(Long userId, Long productId, int quantity);
    void addItemToCartForCurrentUser(HttpServletRequest request, Long productId, int quantity);
    
    void removeItemFromCartForUser(Long userId, Long productId);
    void removeItemFromCartForCurrentUser(HttpServletRequest request, Long productId);
    
    void updateItemInCartForUser(Long userId, Long productId, int quantity);
    void updateItemInCartForCurrentUser(HttpServletRequest request, Long productId, int quantity);
    
    CartItem getCartItemForUser(Long userId, Long productId);
    CartItem getCartItemForCurrentUser(HttpServletRequest request, Long productId);
}
