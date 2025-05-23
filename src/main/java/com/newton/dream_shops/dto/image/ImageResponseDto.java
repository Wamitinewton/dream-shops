package com.newton.dream_shops.dto.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {
    private Long imageId;
    private String imageName;
    private String fileType;
    private String imageUrl;
}
