package com.newton.dream_shops.dto.firebase;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FirebaseFileDto {
    private String downloadUrl;
    private String storagePath;
}
