package com.newton.dream_shops.repository;

import com.newton.dream_shops.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
