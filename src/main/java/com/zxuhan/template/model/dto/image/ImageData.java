package com.zxuhan.template.model.dto.image;

import lombok.Builder;
import lombok.Data;

import java.util.Base64;

/**
 * Image data wrapper class for handling image data from different sources (bytes, URL, base64, etc.)
 */
@Data
@Builder
public class ImageData {

    /**
     * Raw image bytes
     */
    private byte[] bytes;

    /**
     * Image URL (external URL or base64 data URL)
     */
    private String url;

    /**
     * MIME type (e.g. image/png, image/jpeg, image/svg+xml)
     */
    private String mimeType;

    /**
     * Data type
     */
    private DataType dataType;

    /**
     * Data type enum
     */
    public enum DataType {
        /**
         * Raw bytes
         */
        BYTES,
        /**
         * External URL
         */
        URL,
        /**
         * base64 data URL
         */
        DATA_URL
    }

    /**
     * Creates an ImageData from an external URL.
     *
     * @param url external URL
     * @return ImageData instance
     */
    public static ImageData fromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Detect base64 data URL
        if (url.startsWith("data:")) {
            return fromDataUrl(url);
        }

        return ImageData.builder()
                .url(url)
                .dataType(DataType.URL)
                .build();
    }

    /**
     * Creates an ImageData from a base64 data URL.
     *
     * @param dataUrl base64 data URL
     * @return ImageData instance
     */
    public static ImageData fromDataUrl(String dataUrl) {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            return null;
        }

        // Parse data URL format: data:image/png;base64,xxxxx
        String mimeType = "image/png";
        int mimeEnd = dataUrl.indexOf(";");
        if (mimeEnd > 5) {
            mimeType = dataUrl.substring(5, mimeEnd);
        }

        return ImageData.builder()
                .url(dataUrl)
                .mimeType(mimeType)
                .dataType(DataType.DATA_URL)
                .build();
    }

    /**
     * Creates an ImageData from raw bytes.
     *
     * @param bytes    raw image bytes
     * @param mimeType MIME type
     * @return ImageData instance
     */
    public static ImageData fromBytes(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return ImageData.builder()
                .bytes(bytes)
                .mimeType(mimeType != null ? mimeType : "image/png")
                .dataType(DataType.BYTES)
                .build();
    }

    /**
     * Returns raw image bytes. Decodes base64 if the data type is DATA_URL.
     *
     * @return image bytes
     */
    public byte[] getImageBytes() {
        if (dataType == DataType.BYTES) {
            return bytes;
        }

        if (dataType == DataType.DATA_URL && url != null) {
            // Decode base64 data URL
            int base64Start = url.indexOf(",");
            if (base64Start > 0) {
                String base64Data = url.substring(base64Start + 1);
                return Base64.getDecoder().decode(base64Data);
            }
        }

        return null;
    }

    /**
     * Returns whether this instance contains valid data.
     *
     * @return true if valid
     */
    public boolean isValid() {
        return switch (dataType) {
            case BYTES -> bytes != null && bytes.length > 0;
            case URL, DATA_URL -> url != null && !url.isEmpty();
        };
    }

    /**
     * Returns the file extension for the given MIME type (including the leading dot).
     *
     * @return file extension
     */
    public String getFileExtension() {
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
}
