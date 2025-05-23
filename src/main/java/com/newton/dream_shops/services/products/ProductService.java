package com.newton.dream_shops.services.products;

import com.newton.dream_shops.dto.image.ImageResponseDto;
import com.newton.dream_shops.dto.product.ProductDto;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.category.Category;
import com.newton.dream_shops.models.image.Image;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.category.CategoryRepository;
import com.newton.dream_shops.repository.image.ImageRepository;
import com.newton.dream_shops.repository.product.ProductRepository;
import com.newton.dream_shops.request.AddProductsRequest;
import com.newton.dream_shops.request.ProductsUpdateRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    @Override
    public Product addProduct(AddProductsRequest request) {
        // check if the category is found in the database
        // If yes, set it as the new product category
        // If No, save it as a new category
        // Then set is as the new product category

        /**
         * A container object which may or may not contain a non-null value.
         * If a value is present, isPresent() returns true.
         *  If no value is present, the object is considered empty and isPresent() returns false
         */
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        return productRepository.save(createProduct(request, category));
    }

    private Product createProduct(AddProductsRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getPrice(),
                request.getInventory(),
                category
        );
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new CustomException("Product Not Found"));
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException("Category Not Found"));
        return productRepository.findByCategory(category);

    }


    @Override
    public Product updateProduct(ProductsUpdateRequest productsUpdateRequest, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct, productsUpdateRequest))
                .map(productRepository::save)
                .orElseThrow(() -> new CustomException("Product Not Found"));
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
    public void deleteProduct(Long id) {
        productRepository.findById(id).ifPresentOrElse(productRepository::delete, () -> {
            throw new CustomException("Product Not Found");
        });
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
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
        List<ImageResponseDto> imageResponseDtos = images.stream().
                map(image -> modelMapper.map(image, ImageResponseDto.class))
                .toList();

        productDto.setImages(imageResponseDtos);
        return productDto;
    }
}
