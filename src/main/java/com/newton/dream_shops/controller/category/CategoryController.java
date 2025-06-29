package com.newton.dream_shops.controller.category;

import com.newton.dream_shops.exception.AlreadyExistsException;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.category.Category;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.category.ICategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@RestController
@RequestMapping("/${api.prefix}/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved all categories", categories));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addCategory(@RequestBody Category category) {
        try {
            Category newCategory = categoryService.addCategory(category);
            return ResponseEntity.ok(new ApiResponse("Successfully added category", newCategory));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/category/id/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved category", category));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/category/name/{name}")
    public ResponseEntity<ApiResponse> getCategoryByName(@PathVariable String name) {
        try {
            Category category = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved category", category));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/category/{id}/delete")
    public ResponseEntity<ApiResponse> deleteCategoryById(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(new ApiResponse("Successfully deleted category", null));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/category/{id}/update")
    public ResponseEntity<ApiResponse> updateCategoryById(@PathVariable Long id, @RequestBody Category category) {
        try {
            Category newCategory = categoryService.updateCategory(category, id);
            return ResponseEntity.ok(new ApiResponse("Successfully updated category", newCategory));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchCategories(@RequestParam("q") String searchTerm){
        try {
            List<Category> categories = categoryService.searchCategories(searchTerm);
            String message = categories.isEmpty() ?
            "No categories found Matching: " + searchTerm :
            "Found " + categories.size() + " categories";
            return ResponseEntity.ok(new ApiResponse(message, categories));
        } catch (Exception e) {
           return ResponseEntity.status(INTERNAL_SERVER_ERROR)
           .body(new ApiResponse("Search error" +e.getMessage(), null));
        }
    }
}
