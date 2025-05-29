package com.newton.dream_shops.services.cart;

import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.auth.User;
import com.newton.dream_shops.models.cart.Cart;
import com.newton.dream_shops.repository.auth.UserRepository;
import com.newton.dream_shops.repository.cart.CartItemRepository;
import com.newton.dream_shops.repository.cart.CartRepository;
import com.newton.dream_shops.util.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final JwtHelperService jwtHelperService;

    @Override
    @Transactional
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Cart getOrCreateCartForUser(Long userId) {
        Cart exisitingCart = getCartByUserId(userId);
        if (exisitingCart != null) {
            return exisitingCart;
        }

        User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException("User not found"));

        Cart newCart = new Cart();
        newCart.setUser(user);
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional
    public Cart getOrCreateCartForCurrentUser(HttpServletRequest request) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        return getOrCreateCartForUser(userId);
    }

    @Override
    @Transactional
    public void clearCartForUser(Long userId) {
        Cart cart = getCartByUserId(userId);
        if (cart != null) {
            cartItemRepository.deleteAllByCartId(cart.getId());
            cart.getCartItems().clear();
            cart.setTotalAmount(BigDecimal.ZERO);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public void clearCartForCurrentUser(HttpServletRequest request) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User Authentication required");
        }
        clearCartForUser(userId);
        }

    @Override
    public BigDecimal getTotalForUser(Long userId) {
        Cart cart = getCartByUserId(userId);
        return cart != null ? cart.getTotalAmount() : BigDecimal.ZERO;
        }

    @Override
    public BigDecimal getTotalForCurrentUser(HttpServletRequest request) {
        Long userId =jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        return getTotalForUser(userId);
        }
}
