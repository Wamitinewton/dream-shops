package com.newton.dream_shops.services.products;

import com.newton.dream_shops.dto.product.AddProductsRequest;
import com.newton.dream_shops.dto.product.ProductDto;
import com.newton.dream_shops.dto.product.ProductsUpdateRequest;
import com.newton.dream_shops.models.product.Product;

import java.util.List;

public interface IProductService {
    ProductDto addProduct(AddProductsRequest product);

    ProductDto getProductById(Long id);

    List<ProductDto> getProductsByCategoryId(Long categoryId);

    ProductDto updateProduct(ProductsUpdateRequest product, Long productId);

    void deleteProduct(Long id);

    List<ProductDto> getAllProducts();

    List<ProductDto> getProductsByCategory(String category);

    List<ProductDto> getProductByBrand(String brand);

    List<ProductDto> getProductByCategoryAndBrand(String category, String brand);

    List<ProductDto> getProductByName(String name);

    List<ProductDto> getProductByBrandAndName(String brand, String name);

    Long countProductsByBrandAndName(String brand, String name);

    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto toProductDto(Product product);
}
