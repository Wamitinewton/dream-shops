package com.newton.dream_shops.request;

import com.newton.dream_shops.models.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductsUpdateRequest {
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private int inventory;
    private Category category;
}
