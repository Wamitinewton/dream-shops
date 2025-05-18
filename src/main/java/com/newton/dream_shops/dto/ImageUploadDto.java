package com.newton.dream_shops.dto;

import com.newton.dream_shops.models.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDto {
    private MultipartFile file;
    private Long productId;
    private String fileName;
    private String fileType;

    /**
     * Factory Method to create an ImageUploadDto from MultiPartFile
     */
    public static ImageUploadDto fromMultipartFile(MultipartFile file) throws IOException {
        return ImageUploadDto.builder()
                .file(file)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .build();
    }

    /**
     * Creates an ImageUploadDto from MultiPartFile with product ID
     */
    public static ImageUploadDto fromMultipartFile(MultipartFile file, Long productId) throws IOException {
        return ImageUploadDto.builder()
                .file(file)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .productId(productId)
                .build();
    }

    /**
     * Converts this DTO to an Image Entity
     * The imageUrl and storagePath will be set after Firebase upload
     */
    public Image toImageEntity() {
        Image image = new Image();
        image.setFileName(this.fileName);
        image.setFileType(this.fileType);
        return image;
    }

    /**
     * Convert Image entity to ImageResponseDto for client response
     */
    public static ImageResponseDto toImageDto(Image image) {
        return ImageResponseDto.builder()
                .imageId(image.getId())
                .imageName(image.getFileName())
                .fileType(image.getFileType())
                .imageUrl(image.getImageUrl())
                .build();
    }
}