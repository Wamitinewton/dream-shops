package com.newton.dream_shops.services.cart.cartItem;

import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.cart.Cart;
import com.newton.dream_shops.models.cart.CartItem;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.cart.CartItemRepository;
import com.newton.dream_shops.repository.cart.CartRepository;
import com.newton.dream_shops.services.cart.cart.ICartService;
import com.newton.dream_shops.services.products.IProductService;
import com.newton.dream_shops.util.jwt.JwtHelperService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService implements ICartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ICartService cartService;
    private final JwtHelperService jwtHelperService;

    @Override
    @Transactional
    public void addItemToCart(Long userId, Long productId, int quantity) {
        //1. Get or Create The Cart for the user
        //2. Get The Product
        //3. Check if the product is already in the cart
        //4. If yes, then increase the quantity with the requested quantity
        //5. If no, then initiate a new cart item entry

        Cart cart = cartService.getOrCreateCartForUser(userId);
        Product product = productService.getProductById(productId);
        
        CartItem cartItem = cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void addItemToCartForUser(Long userId, Long productId, int quantity) {
        addItemToCart(userId, productId, quantity);
    }

    @Override
    @Transactional
    public void addItemToCartForCurrentUser(HttpServletRequest request, Long productId, int quantity) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        addItemToCartForUser(userId, productId, quantity);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            throw new CustomException("Cart not found for user");
        }
        
        CartItem cartItem = getCartItemForUser(userId, productId);
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCartForUser(Long userId, Long productId) {
        removeItemFromCart(userId, productId);
    }

    @Override
    @Transactional
    public void removeItemFromCartForCurrentUser(HttpServletRequest request, Long productId) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        removeItemFromCartForUser(userId, productId);
    }

    @Override
    @Transactional
    public void updateItemInCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            removeItemFromCart(userId, productId);
            return;
        }

        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            throw new CustomException("Cart not found for user");
        }
        
        cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .ifPresentOrElse(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();
                    cartItemRepository.save(item);
                }, () -> {
                    throw new CustomException("Item not found in cart");
                });

        BigDecimal totalAmount = cart.getCartItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateItemInCartForUser(Long userId, Long productId, int quantity) {
        updateItemInCart(userId, productId, quantity);
    }

    @Override
    @Transactional
    public void updateItemInCartForCurrentUser(HttpServletRequest request, Long productId, int quantity) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        updateItemInCartForUser(userId, productId, quantity);
    }

    @Override
    public CartItem getCartItemById(Long userId, Long productId) {
        return getCartItemForUser(userId, productId);
    }

    @Override
    public CartItem getCartItemForUser(Long userId, Long productId) {
        Cart cart = cartService.getCartByUserId(userId);
        if (cart == null) {
            throw new CustomException("Cart not found for user");
        }
        
        return cart.getCartItems()
                .stream()
                .filter(item -> item.getProduct()
                        .getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CustomException("Item not found"));
    }

    @Override
    public CartItem getCartItemForCurrentUser(HttpServletRequest request, Long productId) {
        Long userId = jwtHelperService.getCurrentUserIdFromRequest(request);
        if (userId == null) {
            throw new CustomException("User authentication required");
        }
        return getCartItemForUser(userId, productId);
    }
}