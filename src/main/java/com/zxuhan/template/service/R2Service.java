package com.zxuhan.template.service;

import com.zxuhan.template.config.R2Config;
import com.zxuhan.template.model.dto.image.ImageData;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.UUID;

/**
 * Cloudflare R2 object storage service.
 * Uploads images and files to an R2 bucket via the S3-compatible API.
 */
@Service
@Slf4j
public class R2Service {

    @Resource
    private R2Config r2Config;

    private S3Client s3Client;

    private final OkHttpClient httpClient = new OkHttpClient();

    @PostConstruct
    public void init() {
        String endpoint = "https://" + r2Config.getAccountId() + ".r2.cloudflarestorage.com";
        s3Client = S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2Config.getAccessKeyId(), r2Config.getSecretAccessKey())))
                .build();
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    /**
     * Upload ImageData to R2 (unified entry point).
     * Automatically selects the upload method based on data type.
     */
    public String uploadImageData(ImageData imageData, String folder) {
        if (imageData == null || !imageData.isValid()) {
            log.warn("Invalid ImageData, upload skipped");
            return null;
        }

        try {
            return switch (imageData.getDataType()) {
                case BYTES -> uploadBytes(imageData.getBytes(), imageData.getMimeType(), folder);
                case URL -> uploadFromUrl(imageData.getUrl(), folder);
                case DATA_URL -> uploadFromDataUrl(imageData, folder);
            };
        } catch (Exception e) {
            log.error("Upload ImageData to R2 failed, dataType={}", imageData.getDataType(), e);
            return null;
        }
    }

    /**
     * Upload byte data to R2.
     */
    public String uploadBytes(byte[] bytes, String mimeType, String folder) {
        if (bytes == null || bytes.length == 0) {
            log.warn("Byte data is empty, upload skipped");
            return null;
        }

        try {
            String extension = getExtensionFromMimeType(mimeType);
            String key = folder + "/" + UUID.randomUUID() + extension;
            String contentType = mimeType != null ? mimeType : "image/png";

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Config.getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(bytes));

            String publicUrl = buildPublicUrl(key);
            log.info("Bytes uploaded to R2, size={} bytes, url={}", bytes.length, publicUrl);
            return publicUrl;
        } catch (Exception e) {
            log.error("Upload bytes to R2 failed", e);
            return null;
        }
    }

    /**
     * Download image from external URL and upload to R2.
     */
    public String uploadFromUrl(String imageUrl, String folder) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("Image URL is empty, upload skipped");
            return null;
        }

        try {
            Request request = new Request.Builder().url(imageUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Image download failed: {}, code={}", imageUrl, response.code());
                    return null;
                }

                byte[] imageBytes = response.body().bytes();
                String contentType = response.header("Content-Type", "image/jpeg");
                return uploadBytes(imageBytes, contentType, folder);
            }
        } catch (IOException e) {
            log.error("Upload image from URL to R2 failed: {}", imageUrl, e);
            return null;
        }
    }

    /**
     * Decode a base64 data URL and upload to R2.
     */
    public String uploadFromDataUrl(ImageData imageData, String folder) {
        byte[] bytes = imageData.getImageBytes();
        if (bytes == null || bytes.length == 0) {
            log.warn("Data URL decoding failed, upload skipped");
            return null;
        }
        return uploadBytes(bytes, imageData.getMimeType(), folder);
    }

    /**
     * Upload image to R2 (legacy interface — falls back to original URL on failure).
     */
    public String uploadImage(String imageUrl, String folder) {
        String result = uploadFromUrl(imageUrl, folder);
        return result != null ? result : imageUrl;
    }

    /**
     * @deprecated Use uploadImageData() instead.
     */
    @Deprecated
    public String useDirectUrl(String imageUrl) {
        return imageUrl;
    }

    /**
     * Upload a file to R2.
     */
    public String uploadFile(File file, String folder) {
        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String extension = getFileExtension(file.getName());
            String key = folder + "/" + UUID.randomUUID() + extension;

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(r2Config.getBucket())
                    .key(key)
                    .contentType(getContentType(extension))
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(fileBytes));
            return buildPublicUrl(key);
        } catch (IOException e) {
            log.error("Upload file to R2 failed: {}", file.getName(), e);
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : ".svg";
    }

    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".svg" -> "image/svg+xml";
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return ".png";
        }
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> ".png";
        };
    }

    private String buildPublicUrl(String key) {
        String base = r2Config.getPublicUrl();
        if (base == null || base.isEmpty()) {
            log.warn("r2.public-url is not configured; objects will be uploaded but URL is unresolvable");
            return null;
        }
        return base.replaceAll("/$", "") + "/" + key;
    }
}
