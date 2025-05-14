package com.newton.dream_shops.repository;

import com.newton.dream_shops.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
