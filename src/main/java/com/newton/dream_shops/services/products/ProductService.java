package com.newton.dream_shops.services.products;

import com.newton.dream_shops.constants.CacheConstants;
import com.newton.dream_shops.dto.image.ImageResponseDto;
import com.newton.dream_shops.dto.product.AddProductsRequest;
import com.newton.dream_shops.dto.product.ProductDto;
import com.newton.dream_shops.dto.product.ProductsUpdateRequest;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.category.Category;
import com.newton.dream_shops.models.image.Image;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.category.CategoryRepository;
import com.newton.dream_shops.repository.image.ImageRepository;
import com.newton.dream_shops.repository.product.ProductRepository;
import com.newton.dream_shops.util.cache.CacheKeyGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;
    private final ModelMapper modelMapper;
    private final CacheKeyGenerator cacheKeyGenerator;

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY_AND_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND_AND_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCT_COUNT, allEntries = true)
    })
    public ProductDto addProduct(AddProductsRequest request) {
        // check if the category is found in the database
        // If yes, set it as the new product category
        // If No, save it as a new category
        // Then set is as the new product category

        /**
         * A container object which may or may not contain a non-null value.
         * If a value is present, isPresent() returns true.
         * If no value is present, the object is considered empty and isPresent()
         * returns false
         */
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        Product savedProduct = productRepository.save(createProduct(request, category));
        return toProductDto(savedProduct);
    }

    private Product createProduct(AddProductsRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getPrice(),
                request.getInventory(),
                category);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCT_BY_ID, key = "@cacheKeyGenerator.generateSimpleKey(#id)")
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new CustomException("Product Not Found"));
        return toProductDto(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_CATEGORY, key = "@cacheKeyGenerator.generateSimpleKey(#categoryId)")
    public List<ProductDto> getProductsByCategoryId(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("Category Not Found"));

        List<Product> product = productRepository.findByCategory(category);
        return getConvertedProducts(product);

    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCT_BY_ID, key = "@cacheKeyGenerator.generateSimpleKey(#productId)"),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY_AND_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND_AND_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCT_COUNT, allEntries = true)
    })
    public ProductDto updateProduct(ProductsUpdateRequest productsUpdateRequest, Long productId) {

        Product product = productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, productsUpdateRequest))
                .map(productRepository::save)
                .orElseThrow(() -> new CustomException("Product Not Found"));
        return toProductDto(product);
    }

    private Product updateExistingProduct(Product existingProduct, ProductsUpdateRequest productsUpdateRequest) {
        existingProduct.setName(productsUpdateRequest.getName());
        existingProduct.setBrand(productsUpdateRequest.getBrand());
        existingProduct.setDescription(productsUpdateRequest.getDescription());
        existingProduct.setPrice(productsUpdateRequest.getPrice());
        existingProduct.setInventory(productsUpdateRequest.getInventory());
        Category category = categoryRepository.findByName(productsUpdateRequest.getCategory().getName());
        existingProduct.setCategory(category);
        return existingProduct;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.PRODUCTS, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCT_BY_ID, key = "@cacheKeyGenerator.generateSimpleKey(#id)"),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_CATEGORY_AND_BRAND, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCTS_BY_BRAND_AND_NAME, allEntries = true),
            @CacheEvict(value = CacheConstants.PRODUCT_COUNT, allEntries = true)
    })
    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete, () -> {
            throw new CustomException("Product Not Found");
        });
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS)
    public List<ProductDto> getAllProducts() {
        List<Product> product = productRepository.findAll();
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_CATEGORY, key = "@cacheKeyGenerator.generateSimpleKey(#category)")
    public List<ProductDto> getProductsByCategory(String category) {
        List<Product> product = productRepository.findByCategoryName(category);
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_BRAND, key = "@cacheKeyGenerator.generateSimpleKey(#brand)")
    public List<ProductDto> getProductByBrand(String brand) {
        List<Product> product = productRepository.findByBrand(brand);
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_CATEGORY_AND_BRAND, key = "@cacheKeyGenerator.generateKey(#category, #brand)")
    public List<ProductDto> getProductByCategoryAndBrand(String category, String brand) {
        List<Product> product = productRepository.findByCategoryNameAndBrand(category, brand);
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_NAME, key = "@cacheKeyGenerator.generateSimpleKey(#name)")
    public List<ProductDto> getProductByName(String name) {
        List<Product> product = productRepository.findByName(name);
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCTS_BY_BRAND_AND_NAME, key = "@cacheKeyGenerator.generateKey(#brand, #name)")
    public List<ProductDto> getProductByBrandAndName(String brand, String name) {
        List<Product> product = productRepository.findByBrandAndName(brand, name);
        return getConvertedProducts(product);
    }

    @Override
    @Cacheable(value = CacheConstants.PRODUCT_COUNT, key = "@cacheKeyGenerator.generateKey(#brand, #name)")
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::toProductDto).toList();
    }

    @Override
    public ProductDto toProductDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageResponseDto> imageResponseDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageResponseDto.class))
                .toList();

        productDto.setImages(imageResponseDtos);
        return productDto;
    }
}