package com.newton.dream_shops.dto;

import lombok.Data;

@Data
public class ImageResponseDto {
    private Long imageId;
    private String imageName;
    private String downloadUrl;
}
