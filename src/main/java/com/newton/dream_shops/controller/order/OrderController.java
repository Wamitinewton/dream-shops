package com.newton.dream_shops.controller.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.dto.order.OrderStatusUpdateRequest;
import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.order.IOrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import static org.springframework.http.HttpStatus.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;

    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse> createOrder(HttpServletRequest request) {
        try {
            OrderDto order = orderService.placeOrder(request);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully", order));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while placing order", null));
        }
    }

    @PostMapping("/place-order/user/{userId}")
    public ResponseEntity<ApiResponse> placeOrderForUser(@PathVariable Long userId) {
        try {
            OrderDto order = orderService.placeOrderForUser(userId);
            return ResponseEntity.ok()
                    .body(new ApiResponse("Order placed successfully", order));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while placing order", null));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDto order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving order", null));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse> getUserOrders(HttpServletRequest request) {
        try {
            List<OrderDto> orders = orderService.getUserOrders(request);
            return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving orders", null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrdersByUserId(@PathVariable Long userId) {
        try {
            List<OrderDto> orders = orderService.getUserOrdersByUserId(userId);
            return ResponseEntity.ok(new ApiResponse("User orders retrieved successfully", orders));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving user orders", null));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<OrderDto> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving orders", null));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        try {
            OrderDto updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
            return ResponseEntity.ok(new ApiResponse("Order status updated successfully", updatedOrder));
        } catch (CustomException e) {
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while updating order status", null));
        }
    }

    @GetMapping("/order-statuses")
    public ResponseEntity<ApiResponse> getOrderStatuses() {
        try {
            OrderStatus[] statuses = OrderStatus.values();
            return ResponseEntity.ok(new ApiResponse("Order statuses retrieved successfully", statuses));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving order statuses", null));
        }
    }
}