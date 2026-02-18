package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.zxuhan.template.constant.ArticleConstant.*;

/**
 * Meme / emoji-pack retrieval configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "emoji-pack")
@Data
public class EmojiPackConfig {

    /**
     * Bing image search URL.
     */
    private String searchUrl = BING_IMAGE_SEARCH_URL;

    /**
     * Meme keyword suffix (appended programmatically, not by the AI).
     */
    private String suffix = EMOJI_PACK_SUFFIX;

    /**
     * Request timeout in milliseconds.
     */
    private Integer timeout = 10000;
}
