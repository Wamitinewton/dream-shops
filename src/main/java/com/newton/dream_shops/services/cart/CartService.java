package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.exception.ResourceNotFoundException;
import com.newton.dream_shops.models.Cart;
import com.newton.dream_shops.models.CartItem;
import com.newton.dream_shops.repository.CartItemRepository;
import com.newton.dream_shops.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    @Override
    public Cart getCart(Long id) {
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Not Found"));

        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long id) {
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getCartItems().clear();
        cartRepository.deleteById(id);
    }

    @Override
    public BigDecimal getTotal(Long id) {
        Cart cart = getCart(id);
        return cart.getTotalAmount();
    }
}
