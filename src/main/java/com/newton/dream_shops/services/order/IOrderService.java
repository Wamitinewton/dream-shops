package com.newton.dream_shops.services.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.models.order.Order;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IOrderService {
    
    /**
     * Place an order for the current authenticated user
     * @param request HTTP request containing user authentication
     * @return Created order
     */
    OrderDto placeOrder(HttpServletRequest request);
    
    /**
     * Place an order for a specific user
     * @param userId User ID to place order for
     * @return Created order
     */
    OrderDto placeOrderForUser(Long userId);
    
    /**
     * Update order status
     * @param orderId Order ID to update
     * @param newStatus New status to set
     * @return Updated order
     */
    OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus);
    
    /**
     * Get order by ID
     * @param orderId Order ID
     * @return Order DTO
     */
    OrderDto getOrderById(Long orderId);
    
    /**
     * Get all orders for current authenticated user
     * @param request HTTP request containing user authentication
     * @return List of user's orders
     */
    List<OrderDto> getUserOrders(HttpServletRequest request);
    
    /**
     * Get all orders for a specific user
     * @param userId User ID
     * @return List of user's orders
     */
    List<OrderDto> getUserOrdersByUserId(Long userId);
    
    /**
     * Get orders by status
     * @param status Order status to filter by
     * @return List of orders with specified status
     */
    List<OrderDto> getOrdersByStatus(OrderStatus status);
}