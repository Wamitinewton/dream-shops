package com.newton.dream_shops.services.products;

import com.newton.dream_shops.models.Product;
import com.newton.dream_shops.request.AddProductsRequest;
import com.newton.dream_shops.request.ProductsUpdateRequest;

import java.util.List;

public interface IProductService {
    Product addProduct(AddProductsRequest product);

    Product getProductById(Long id);

    List<Product> getProductsByCategoryId(Long categoryId);

    Product updateProduct(ProductsUpdateRequest product, Long productId);

    void deleteProduct(Long id);

    List<Product> getAllProducts();

    List<Product> getProductsByCategory(String category);

    List<Product> getProductByBrand(String brand);

    List<Product> getProductByCategoryAndBrand(String category, String brand);

    List<Product> getProductByName(String name);

    List<Product> getProductByBrandAndName(String brand, String name);

    Long countProductsByBrandAndName(String brand, String name);
}
