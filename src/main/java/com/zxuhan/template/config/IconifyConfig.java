package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Iconify icon library configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "iconify")
@Data
public class IconifyConfig {

    /**
     * Iconify API base URL.
     */
    private String apiUrl = "https://api.iconify.design";

    /**
     * Maximum number of search results.
     */
    private Integer searchLimit = 10;

    /**
     * Default icon height in pixels.
     */
    private Integer defaultHeight = 64;

    /**
     * Default icon color (leave empty to use currentColor, or set e.g. "#000000").
     */
    private String defaultColor = "";
}
