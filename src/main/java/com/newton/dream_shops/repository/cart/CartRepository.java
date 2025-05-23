package com.newton.dream_shops.repository.cart;

import com.newton.dream_shops.models.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
