package com.newton.dream_shops.dto;

import com.newton.dream_shops.models.Category;
import com.newton.dream_shops.models.Image;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private int inventory;
    private Category category;
    private List<ImageResponseDto> images;
}
