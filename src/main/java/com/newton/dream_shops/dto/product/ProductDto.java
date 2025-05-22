package com.newton.dream_shops.dto.product;

import com.newton.dream_shops.dto.image.ImageResponseDto;
import com.newton.dream_shops.models.category.Category;
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
