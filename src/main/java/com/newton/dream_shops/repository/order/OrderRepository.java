package com.newton.dream_shops.repository.order;

import com.newton.dream_shops.enums.OrderStatus;
import com.newton.dream_shops.models.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

}
