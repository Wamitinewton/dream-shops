package com.newton.dream_shops.dto;

import com.newton.dream_shops.models.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDto {
    private MultipartFile file;
    private Long productId;
    private String fileName;
    private String fileType;
    private byte[] imageData;

    /**
     * Factory Method to create an ImageUploadDto from MultiPartFile
     */

    public static ImageUploadDto fromMultipartFile(MultipartFile file) throws IOException {
        return ImageUploadDto.builder()
                .file(file)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .imageData(file.getBytes())
                .build();
    }

    /**
     * Converts this DTO to an Image Entity
     */

    public Image toImageEntity() throws SQLException {
        Image image = new Image();
        image.setFileName(this.fileName);
        image.setFileType(this.fileType);
        image.setImage(new SerialBlob(this.imageData));
        return image;
    }

    /**
     * Convert Image entity to ImageDto for client response
     */
    public static ImageResponseDto toImageDto(Image image) {
        ImageResponseDto imageResponseDto = new ImageResponseDto();
        imageResponseDto.setImageId(image.getId());
        imageResponseDto.setImageName(image.getFileName());
        imageResponseDto.setDownloadUrl(image.getDownloadUrl());
        return imageResponseDto;
    }
}
