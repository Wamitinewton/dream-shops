package com.newton.dream_shops.services.images;

import com.newton.dream_shops.dto.ImageResponseDto;
import com.newton.dream_shops.dto.ImageUploadDto;
import com.newton.dream_shops.exception.ResourceNotFoundException;
import com.newton.dream_shops.models.Image;
import com.newton.dream_shops.models.Product;
import com.newton.dream_shops.repository.ImageRepository;
import com.newton.dream_shops.services.products.IProductService;
import com.newton.dream_shops.utility.ImageUrlBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService implements IImageService {
    private final ImageRepository imageRepository;
    private final IProductService productService;
    private final ImageUrlBuilder imageUrlBuilder;

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Image Not Found"));
    }

    @Override
    public void deleteImageById(Long id) {
        imageRepository.findById(id).ifPresentOrElse(imageRepository::delete, () -> {
            new ResourceNotFoundException("Image Not Found");
        });
    }

    @Override
    public List<ImageResponseDto> saveImage(List<MultipartFile> files, Long productId) {
        Product product = productService.getProductById(productId);

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
            // Convert MultiPartFile to our DTO
            ImageUploadDto uploadDto = ImageUploadDto.fromMultipartFile(file);

            Image image = uploadDto.toImageEntity();
            image.setProduct(product);

            image.setDownloadUrl(imageUrlBuilder.buildPlaceholderUrl());
            Image savedImage = imageRepository.save(image);

            savedImage.setDownloadUrl(imageUrlBuilder.buildDownloadUrl(savedImage.getId()));
            savedImage = imageRepository.save(savedImage);

            //Convert to response DTO
            return Optional.of(ImageUploadDto.toImageDto(savedImage));
        } catch (IOException | SQLException e) {
            log.error("Error saving image: {}", file.getOriginalFilename(), e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void updateImage(MultipartFile file, Long imageId) {
        Image image = getImageById(imageId);
        try {
            ImageUploadDto uploadDto = ImageUploadDto.fromMultipartFile(file);
            image.setFileName(uploadDto.getFileName());
            image.setFileType(uploadDto.getFileType());
            image.setImage(new SerialBlob(uploadDto.getImageData()));
            imageRepository.save(image);
        } catch (IOException | SQLException e) {
            log.error("Error updating image: {}", imageId, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private String buildDownloadUrl(Long imageId) {
        String basePath = "/api/vi/images/download/";
        return imageId != null ? basePath + imageId : null;
    }
}
