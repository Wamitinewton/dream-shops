package com.newton.dream_shops.utility;

import org.springframework.stereotype.Component;

@Component
public class ImageUrlBuilder {

    private static final String BASE_IMAGE_RL = "/api/v1/images/download/";

    /**
     * Build a download URL for an image based on its ID
     * @param imageId ID of the image
     * @return The full download URL
     */
    public String buildDownloadUrl(Long imageId) {
        if (imageId == null) {
            return BASE_IMAGE_RL;
        }
        return BASE_IMAGE_RL + imageId;
    }

    /**
     * Generate a temporary placeholder URL before an ID is available
     * @return A placeholder URL
     */
    public String buildPlaceholderUrl() {
        return BASE_IMAGE_RL;
    }
}
