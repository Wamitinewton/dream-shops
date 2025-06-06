package com.newton.dream_shops.services.images;

import com.newton.dream_shops.dto.firebase.FirebaseFileDto;
import com.newton.dream_shops.dto.image.ImageResponseDto;
import com.newton.dream_shops.dto.image.ImageUploadDto;
import com.newton.dream_shops.dto.product.ProductDto;
import com.newton.dream_shops.exception.CustomException;
import com.newton.dream_shops.models.image.Image;
import com.newton.dream_shops.models.product.Product;
import com.newton.dream_shops.repository.image.ImageRepository;
import com.newton.dream_shops.repository.product.ProductRepository;
import com.newton.dream_shops.services.firebase.FirebaseStorageService;
import com.newton.dream_shops.services.products.IProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final FirebaseStorageService firebaseStorageService;
    private final ProductRepository productRepository;

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new CustomException("Image Not Found"));
    }

    @Override
    public void deleteImageById(Long id) {
        Image image = imageRepository.findById(id).orElseThrow(() ->
                new CustomException("Image Not Found")
        );

        if (image.getStoragePath() != null) {
            boolean deleted = firebaseStorageService.deleteFile(image.getStoragePath());
            if (!deleted) {
                log.error("Failed to delete image from firebase storage: {}", image.getStoragePath());
            }
        }
    }

    @Override
    public List<ImageResponseDto> saveImage(List<MultipartFile> files, Long productId) {
         ProductDto productDto = productService.getProductById(productId);
        Product product = productRepository.findById(productDto.getId())
                .orElseThrow(() -> new CustomException("Product Not Found"));

        return files.stream()
                .map(file -> processAndSaveImage(file, product))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Process and save a single image file
     */

    private Optional<ImageResponseDto> processAndSaveImage(MultipartFile file, Product product) {
        try {
            ImageUploadDto uploadDto = ImageUploadDto.fromMultipartFile(file);
            Image image = uploadDto.toImageEntity();
            image.setProduct(product);

            //Upload to Firebase
            FirebaseFileDto firebaseFileDto = firebaseStorageService.uploadFile(file, image.getFileName());
            image.setImageUrl(firebaseFileDto.getDownloadUrl());
            image.setStoragePath(firebaseFileDto.getStoragePath());

            Image savedImage = imageRepository.save(image);

            return Optional.of(ImageUploadDto.toImageDto(savedImage));
        } catch (IOException e) {
            log.error("Error uploading image: {}", file.getOriginalFilename(), e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void updateImage(MultipartFile file, Long imageId) {
        Image image = getImageById(imageId);

        try {
            //Delete old file from Firebase first
            if (image.getStoragePath() != null) {
                firebaseStorageService.deleteFile(image.getStoragePath());
            }

            ImageUploadDto uploadDto = ImageUploadDto.fromMultipartFile(file);

            FirebaseFileDto firebaseFileDto = firebaseStorageService.uploadFile(file, uploadDto.getFileName());
            image.setFileName(uploadDto.getFileName());
            image.setFileType(uploadDto.getFileType());
            image.setImageUrl(firebaseFileDto.getDownloadUrl());
            image.setStoragePath(firebaseFileDto.getStoragePath());

            imageRepository.save(image);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
