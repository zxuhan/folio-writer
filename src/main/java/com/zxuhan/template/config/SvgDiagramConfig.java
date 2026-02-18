package com.zxuhan.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import static com.zxuhan.template.constant.ArticleConstant.*;

/**
 * SVG conceptual diagram generation configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "svg-diagram")
@Data
public class SvgDiagramConfig {

    /**
     * Default width.
     */
    private Integer defaultWidth = SVG_DEFAULT_WIDTH;

    /**
     * Default height.
     */
    private Integer defaultHeight = SVG_DEFAULT_HEIGHT;

    /**
     * COS storage folder.
     */
    private String folder = "svg-diagrams";
}
