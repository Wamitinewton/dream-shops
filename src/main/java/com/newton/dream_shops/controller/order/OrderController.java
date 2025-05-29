package com.newton.dream_shops.controller.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.dto.order.OrderStatusUpdateRequest;
import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.order.Order;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.order.IOrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.HttpStatus.*;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/orders")
@Slf4j
public class OrderController {
    private final IOrderService orderService;

    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse> createOrder(HttpServletRequest request) {
        try {
            log.info("Received request to place order");
            Order order = orderService.placeOrder(request);
            log.info("Order placed successfully with ID: {}", order.getId());
            return ResponseEntity.ok(new ApiResponse("Order placed successfully", order));
        } catch (CustomException e) {
            log.error("Custom exception while placing order: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while placing order", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while placing order", null));
        }
    }

    @PostMapping("/place-order/user/{userId}")
    public ResponseEntity<ApiResponse> placeOrderForUser(@PathVariable Long userId) {
        try {
            log.info("Received request to place order for user: {}", userId);
            Order order = orderService.placeOrderForUser(userId);
            log.info("Order placed successfully for user {} with ID: {}", userId, order.getId());
            return ResponseEntity.ok()
                    .body(new ApiResponse("Order placed successfully", order));
        } catch (CustomException e) {
            log.error("Custom exception while placing order for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while placing order for user {}", userId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while placing order", null));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            log.info("Retrieving order with ID: {}", orderId);
            OrderDto order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(new ApiResponse("Order retrieved successfully", order));
        } catch (CustomException e) {
            log.error("Custom exception while retrieving order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while retrieving order {}", orderId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving order", null));
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse> getUserOrders(HttpServletRequest request) {
        try {
            log.info("Retrieving orders for authenticated user");
            List<OrderDto> orders = orderService.getUserOrders(request);
            return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
        } catch (CustomException e) {
            log.error("Custom exception while retrieving user orders: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while retrieving user orders", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving orders", null));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrdersByUserId(@PathVariable Long userId) {
        try {
            log.info("Retrieving orders for user: {}", userId);
            List<OrderDto> orders = orderService.getUserOrdersByUserId(userId);
            return ResponseEntity.ok(new ApiResponse("User orders retrieved successfully", orders));
        } catch (CustomException e) {
            log.error("Custom exception while retrieving orders for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while retrieving orders for user {}", userId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving user orders", null));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            log.info("Retrieving orders with status: {}", status);
            List<OrderDto> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(new ApiResponse("Orders retrieved successfully", orders));
        } catch (CustomException e) {
            log.error("Custom exception while retrieving orders by status {}: {}", status, e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while retrieving orders by status {}", status, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving orders", null));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        try {
            log.info("Updating status for order {} to {}", orderId, request.getStatus());
            Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
            return ResponseEntity.ok(new ApiResponse("Order status updated successfully", updatedOrder));
        } catch (CustomException e) {
            log.error("Custom exception while updating order status: {}", e.getMessage());
            return ResponseEntity.status(BAD_REQUEST)
                    .body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error while updating order status for order {}", orderId, e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while updating order status", null));
        }
    }

    @GetMapping("/order-statuses")
    public ResponseEntity<ApiResponse> getOrderStatuses() {
        try {
            log.info("Retrieving all order statuses");
            OrderStatus[] statuses = OrderStatus.values();
            return ResponseEntity.ok(new ApiResponse("Order statuses retrieved successfully", statuses));
        } catch (Exception e) {
            log.error("Unexpected error while retrieving order statuses", e);
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An unexpected error occurred while retrieving order statuses", null));
        }
    }
}