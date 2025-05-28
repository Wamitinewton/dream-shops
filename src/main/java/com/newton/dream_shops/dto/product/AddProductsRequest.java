package com.newton.dream_shops.dto.product;

import com.newton.dream_shops.models.category.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductsRequest {
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private int inventory;
    private Category category;
}
