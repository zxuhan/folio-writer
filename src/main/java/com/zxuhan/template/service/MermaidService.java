package com.zxuhan.template.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;
import com.zxuhan.template.config.MermaidConfig;
import com.zxuhan.template.model.dto.image.ImageData;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.File;

import static com.zxuhan.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Mermaid diagram generation service.
 * Converts Mermaid code to images using mermaid-cli.
 */
@Service
@Slf4j
public class MermaidService implements ImageSearchService {

    @Resource
    private MermaidConfig mermaidConfig;

    @Override
    public String searchImage(String keywords) {
        // For Mermaid, keywords is the Mermaid code.
        // This method is deprecated; use getImageData() instead.
        ImageData imageData = generateDiagramData(keywords);
        // Return null; URL is no longer returned directly
        return null;
    }

    @Override
    public String getImage(ImageRequest request) {
        // This method is deprecated; use getImageData() instead.
        // Return null; upload logic is handled by ImageServiceStrategy.
        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        // Prefer prompt (Mermaid code); fall back to keywords
        String mermaidCode = request.getEffectiveParam(true);
        return generateDiagramData(mermaidCode);
    }

    /**
     * Generate Mermaid diagram data
     *
     * @param mermaidCode Mermaid code
     * @return image byte data, or null on failure
     */
    public ImageData generateDiagramData(String mermaidCode) {
        if (mermaidCode == null || mermaidCode.trim().isEmpty()) {
            log.warn("Mermaid code is empty");
            return null;
        }

        File tempInputFile = null;
        File tempOutputFile = null;

        try {
            // Create temporary input file
            tempInputFile = FileUtil.createTempFile("mermaid_input_", ".mmd", true);
            FileUtil.writeUtf8String(mermaidCode, tempInputFile);

            // Create temporary output file
            String outputExtension = "." + mermaidConfig.getOutputFormat();
            tempOutputFile = FileUtil.createTempFile("mermaid_output_", outputExtension, true);

            // Convert to image
            convertMermaidToImage(tempInputFile, tempOutputFile);

            // Check output file
            if (!tempOutputFile.exists() || tempOutputFile.length() == 0) {
                log.error("Mermaid CLI execution failed: output file missing or empty");
                return null;
            }

            // Read image bytes
            byte[] imageBytes = FileUtil.readBytes(tempOutputFile);
            String mimeType = getMimeType(mermaidConfig.getOutputFormat());

            log.info("Mermaid diagram generated, size={} bytes", imageBytes.length);
            return ImageData.fromBytes(imageBytes, mimeType);

        } catch (Exception e) {
            log.error("Mermaid diagram generation failed", e);
            return null;
        } finally {
            // Clean up temp files
            if (tempInputFile != null) {
                FileUtil.del(tempInputFile);
            }
            if (tempOutputFile != null) {
                FileUtil.del(tempOutputFile);
            }
        }
    }

    /**
     * Get MIME type for the given output format
     */
    private String getMimeType(String format) {
        return switch (format.toLowerCase()) {
            case "png" -> "image/png";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            default -> "image/png";
        };
    }

    /**
     * Invoke Mermaid CLI to convert code to image
     */
    private void convertMermaidToImage(File inputFile, File outputFile) {
        try {
            // Select command based on OS
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : mermaidConfig.getCliCommand();

            // Build command line
            String cmdLine = String.format("%s -i %s -o %s -b %s",
                    command,
                    inputFile.getAbsolutePath(),
                    outputFile.getAbsolutePath(),
                    mermaidConfig.getBackgroundColor()
            );

            // Append width parameter if configured
            if (mermaidConfig.getWidth() != null && mermaidConfig.getWidth() > 0) {
                cmdLine += " -w " + mermaidConfig.getWidth();
            }

            log.info("Executing Mermaid CLI command: {}", cmdLine);

            // Execute command
            String result = RuntimeUtil.execForStr(cmdLine);

            log.debug("Mermaid CLI output: {}", result);

        } catch (Exception e) {
            log.error("Mermaid CLI execution failed", e);
            throw new RuntimeException("Mermaid CLI execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MERMAID;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    @Override
    public boolean isAvailable() {
        try {
            // Check if mermaid-cli is installed
            String command = SystemUtil.getOsInfo().isWindows() ? "mmdc.cmd" : mermaidConfig.getCliCommand();
            String checkCmd = command + " --version";
            String version = RuntimeUtil.execForStr(checkCmd);
            log.info("Mermaid CLI version: {}", version);
            return version != null && !version.isEmpty();
        } catch (Exception e) {
            log.warn("Mermaid CLI unavailable: {}", e.getMessage());
            return false;
        }
    }
}
