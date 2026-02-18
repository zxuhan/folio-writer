package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mermaid diagram generation configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "mermaid")
@Data
public class MermaidConfig {

    /**
     * CLI command (mmdc.cmd on Windows, mmdc on Linux/Mac).
     */
    private String cliCommand = "mmdc";

    /**
     * Background color (use "transparent" for a transparent background).
     */
    private String backgroundColor = "transparent";

    /**
     * Output format (svg/png/pdf).
     */
    private String outputFormat = "svg";

    /**
     * Image width in pixels.
     */
    private Integer width = 800;

    /**
     * Command execution timeout in milliseconds.
     */
    private Long timeout = 30000L;
}
