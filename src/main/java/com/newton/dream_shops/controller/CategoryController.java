package com.newton.dream_shops.controller;

import com.newton.dream_shops.models.Category;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.category.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved all categories", categories));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to retrieve all categories", e.getMessage()));
        }
    }

    public ResponseEntity<ApiResponse> addCategory(@RequestBody Category category) {
        try {
            Category newCategory = categoryService.addCategory(category);
            return ResponseEntity.ok(new ApiResponse("Successfully added category", newCategory));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to add category", e.getMessage()));
        }
    }
}
