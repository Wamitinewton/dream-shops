package com.newton.dream_shops.services.cart.cart;

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
    public Cart getCartByUserId(Long userId) {
        Cart cart = cartRepository.findCartByUserId(userId);

        if (cart == null) {
            return null;
        }
        return cart;
    }

    @Override
    @Transactional
    public Cart getOrCreateCartForUser(Long userId) {

        Cart cart = cartRepository.findCartByUserId(userId);

        if (cart != null) {
            return cart;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with id: " + userId));

        cart = new Cart();
        cart.setUser(user);
        cart.setTotalAmount(BigDecimal.ZERO);

        Cart savedCart = cartRepository.save(cart);
        return savedCart;
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
            Long cartId = cart.getId();
            cartItemRepository.deleteAllByCartId(cartId);
            cartRepository.flush();

            Cart refreshedCart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CustomException("Cart not found after clearing items"));
            refreshedCart.getCartItems().clear();
            refreshedCart.setTotalAmount(BigDecimal.ZERO);
            cartRepository.save(refreshedCart);

        } else {
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
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        return getTotalForUser(userId);
    }
}
