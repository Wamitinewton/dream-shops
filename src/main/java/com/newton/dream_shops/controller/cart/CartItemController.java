package com.newton.dream_shops.controller.cart;

import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.cart.cartItem.ICartItemService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/cartItems")
public class CartItemController {
    private final ICartItemService cartItemService;
    @PostMapping("/item/add")
    public ResponseEntity<ApiResponse> addItemToCart(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            HttpServletRequest request
            ) {
        try {
            if (quantity <= 0) {
                return ResponseEntity.status(BAD_REQUEST)
                .body(new ApiResponse("Quantity must be greater than 0", null));
            }
            cartItemService.addItemToCartForCurrentUser(request, productId, quantity);
            return ResponseEntity.ok(new ApiResponse("Successfully added item to cart", null));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED)
                .body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/item/{productId}/remove")
    public ResponseEntity<ApiResponse> removeItemFromCart(
        @PathVariable Long productId,
        HttpServletRequest request
    ) {
        try {
            cartItemService.removeItemFromCartForCurrentUser(request, productId);
            return ResponseEntity.ok(new ApiResponse("Successfully removed item from cart", null));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/item/{productId}/update")
    public ResponseEntity<ApiResponse> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            HttpServletRequest request) {
        try {
            if (quantity < 0) {
                return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse("Quantity cannot be negative", null));
            }
            cartItemService.updateItemInCartForCurrentUser(request, productId, quantity);
            String message = quantity == 0 ? "Item removed from cart" : "Successfully updated item quantity";
            return ResponseEntity.ok(new ApiResponse(message, null));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/item/{productId}")
    public ResponseEntity<ApiResponse> getCartItem(
            @PathVariable Long productId,
            HttpServletRequest request) {
        try {
            var cartItem = cartItemService.getCartItemForCurrentUser(request, productId);
            return ResponseEntity.ok(new ApiResponse("Cart item retrieved successfully", cartItem));
        } catch (CustomException e) {
            if (e.getMessage().contains("authentication")) {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
            }
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
