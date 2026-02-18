package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudflare R2 (S3-compatible object storage) configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "r2")
@Data
public class R2Config {

    /** R2 API token access key id */
    private String accessKeyId;

    /** R2 API token secret */
    private String secretAccessKey;

    /** Cloudflare account id (used to build the S3 endpoint) */
    private String accountId;

    /** Bucket name */
    private String bucket;

    /** Public base URL for objects (custom domain or pub-<hash>.r2.dev). No trailing slash. */
    private String publicUrl;
}
