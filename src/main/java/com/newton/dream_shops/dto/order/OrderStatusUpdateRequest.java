package com.newton.dream_shops.dto.order;

import com.newton.dream_shops.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusUpdateRequest {
    private OrderStatus status;
}
