package com.newton.dream_shops.services.firebase;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.newton.dream_shops.dto.FirebaseFileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FirebaseStorageService {

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    private Storage storage;

    @PostConstruct
    private void initializeFirebase() throws Exception {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new ClassPathResource(credentialsPath).getInputStream());

            storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            log.info("Firebase Storage initialized");
        } catch (Exception e) {
            log.error("Error initializing Firebase Storage", e);
            throw e;
        }
    }

    /**
     * Upload file to Firebase Storage
     * @param file The file to upload
     * @param fileName Optional custom file name
     * @return Object containing the download URL and storage path
     */

    public FirebaseFileDto uploadFile(MultipartFile file, String fileName) throws Exception {
        String originalFileName = fileName != null ? fileName : file.getOriginalFilename();

        //Create a unique name to avoid collisions
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        // Define the Storage path (products/fileName)
        String storagePath = "products/" + uniqueFileName;

        // Create a blob ID and info

        BlobId blobId = BlobId.of(bucketName, storagePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        //Generate the download URL (valid for 10 years....)
        String downloadUrl = storage.signUrl(blobInfo, 10 * 365, TimeUnit.DAYS).toString();

        return new FirebaseFileDto(downloadUrl, storagePath);
    }


    /**
     * Delete file from Firebase Storage
     * @param storagePath The path of the file in Firebase Storage
     * @return True if deletion was successful
     */

    public boolean deleteFile(String storagePath) {
        try {
            BlobId blobId = BlobId.of(bucketName, storagePath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Successfully deleted file from Firebase Storage: {}", storagePath);
            } else {
                log.warn("File not found in Firebase Storage: {}", storagePath);
            }

            return deleted;
        } catch (Exception e) {
            log.error("Error deleting file from Firebase Storage: {}", storagePath, e);
            return false;
        }
    }
}
