package com.newton.dream_shops.controller.cart;

import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.cart.Cart;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.cart.ICartService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/carts")
public class CartController {
    private final ICartService cartService;

    @GetMapping("/my-cart")
    public ResponseEntity<ApiResponse> getCart(HttpServletRequest request) {
        try {
            Cart cart = cartService.getOrCreateCartForCurrentUser(request);
            return ResponseEntity.ok(new ApiResponse("Successfully got cart", cart));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearMyCart(HttpServletRequest request) {
        try {
            cartService.clearCartForCurrentUser(request);
            return ResponseEntity.ok(new ApiResponse("Successfully cleared cart", null));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<ApiResponse> getMyCartTotal(HttpServletRequest request) {
        try {
            BigDecimal totalPrice = cartService.getTotalForCurrentUser(request);
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved total price", totalPrice));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/items-count")
    public ResponseEntity<ApiResponse> getMyCartItemsCount(HttpServletRequest request) {
        try {
            Cart cart = cartService.getOrCreateCartForCurrentUser(request);
            int itemsCount = cart.getCartItems() != null ? cart.getCartItems().size() : 0;
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved items count", itemsCount));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

}
