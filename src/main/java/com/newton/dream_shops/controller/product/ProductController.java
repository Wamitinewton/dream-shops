package com.newton.dream_shops.controller.product;

import com.newton.dream_shops.dto.product.AddProductsRequest;
import com.newton.dream_shops.dto.product.ProductDto;
import com.newton.dream_shops.dto.product.ProductsUpdateRequest;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.response.ApiResponse;
import com.newton.dream_shops.services.products.IProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@AllArgsConstructor
@RestController
@RequestMapping("/${api.prefix}/products")
public class ProductController {
    private final IProductService productService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllProducts() {
        try {
            List<ProductDto> products = productService.getAllProducts();
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved all products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/product/{id}/product")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            ProductDto product = productService.getProductById(id);
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved product", product));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(@RequestBody AddProductsRequest product) {
        try {
            ProductDto newProduct = productService.addProduct(product);
            return ResponseEntity.ok(new ApiResponse("Successfully added product", newProduct));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PutMapping("/product/{id}/update")
    public ResponseEntity<ApiResponse> updateProduct(@RequestBody ProductsUpdateRequest product,
            @PathVariable Long id) {
        try {
            ProductDto updatedProduct = productService.updateProduct(product, id);
            return ResponseEntity.ok(new ApiResponse("Successfully updated product", updatedProduct));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/product/{id}/delete")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse("Successfully deleted product", null));
        } catch (CustomException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/products/by/brand-and-name")
    public ResponseEntity<ApiResponse> getProductByBrandAndName(@RequestParam String productName,
            @RequestParam String brandName) {
        try {
            List<ProductDto> products = productService.getProductByBrandAndName(brandName, productName);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/products/by/category-and-brand")
    public ResponseEntity<ApiResponse> getProductByCategoryAndBrand(@RequestParam String category,
            @RequestParam String brandName) {
        try {
            List<ProductDto> products = productService.getProductByCategoryAndBrand(category, brandName);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/products/{name}/products")
    public ResponseEntity<ApiResponse> getProductByName(@PathVariable String name) {
        try {
            List<ProductDto> products = productService.getProductByName(name);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/products/by-brand")
    public ResponseEntity<ApiResponse> getProductByBrand(@RequestParam String brand) {
        try {
            List<ProductDto> products = productService.getProductByBrand(brand);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/products/{category}/all/products")
    public ResponseEntity<ApiResponse> findProductsByCategory(@PathVariable String category) {
        try {
            List<ProductDto> products = productService.getProductsByCategory(category);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/product/count/by-brand/and-name")
    public ResponseEntity<ApiResponse> countProductsByBrandAndName(@RequestParam String brand,
            @RequestParam String name) {
        try {
            var productCount = productService.countProductsByBrandAndName(brand, name);
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved product count", productCount));
        } catch (Exception e) {
            return ResponseEntity.ok().body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/product/get-by-category-id/{id}/products")
    public ResponseEntity<ApiResponse> getProductsByCategoryId(@PathVariable Long id) {
        try {
            List<ProductDto> products = productService.getProductsByCategoryId(id);
            if (products.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Product not found", null));
            }
            return ResponseEntity.ok(new ApiResponse("Successfully retrieved products", products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

}