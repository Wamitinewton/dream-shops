package com.newton.dream_shops.dto.auth;

import java.util.List;

import com.newton.dream_shops.dto.cart.CartDto;
import com.newton.dream_shops.dto.order.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private List<OrderDto> orders;
    private CartDto cart;
}
