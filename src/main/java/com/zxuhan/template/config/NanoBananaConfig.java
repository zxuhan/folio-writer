package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Nano Banana (Gemini native image generation) configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "nano-banana")
@Data
public class NanoBananaConfig {

    /**
     * Gemini API Key.
     */
    private String apiKey;

    /**
     * Model name.
     * gemini-2.5-flash-image: fast, suitable for high-throughput, low-latency workloads.
     * gemini-3-pro-image-preview: professional grade, supports advanced reasoning and high resolution.
     */
    private String model = "gemini-2.5-flash-image";

    /**
     * Image aspect ratio.
     * Supported values: 1:1, 2:3, 3:2, 3:4, 4:3, 4:5, 5:4, 9:16, 16:9, 21:9
     */
    private String aspectRatio = "16:9";

    /**
     * Image resolution (only supported by gemini-3-pro-image-preview).
     * Supported values: 1K, 2K, 4K
     */
    private String imageSize = "1K";

    /**
     * Output image format.
     * Supported values: image/jpeg, image/png
     */
    private String outputMimeType = "image/png";
}
