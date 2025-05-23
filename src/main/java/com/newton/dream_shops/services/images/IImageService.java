package com.newton.dream_shops.services.images;

import com.newton.dream_shops.dto.image.ImageResponseDto;
import com.newton.dream_shops.models.image.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);

    void deleteImageById(Long id);

    List<ImageResponseDto> saveImage(List<MultipartFile> files, Long productId);

    void updateImage(MultipartFile file, Long imageId);
}
