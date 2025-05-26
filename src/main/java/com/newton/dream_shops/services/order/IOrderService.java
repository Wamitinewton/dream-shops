package com.newton.dream_shops.services.order;

import com.newton.dream_shops.dto.order.OrderDto;
import com.newton.dream_shops.models.order.Order;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrderById(Long orderId);

    List<OrderDto> getUserOrders(Long userId);
}
