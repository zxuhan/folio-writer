package com.zxuhan.template.service;

import cn.hutool.core.util.StrUtil;
import com.zxuhan.template.config.SvgDiagramConfig;
import com.zxuhan.template.constant.PromptConstant;
import com.zxuhan.template.model.dto.image.ImageData;
import com.zxuhan.template.model.dto.image.ImageRequest;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

import static com.zxuhan.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * SVG conceptual diagram generation service.
 * Uses AI to generate SVG code; suitable for concept diagrams, mind-map styles, and relationship visualizations.
 */
@Service
@Slf4j
public class SvgDiagramService implements ImageSearchService {

    @Resource
    private SvgDiagramConfig svgDiagramConfig;

    @Resource
    private ChatModel chatModel;

    @Override
    public String searchImage(String keywords) {
        // This method is deprecated; use getImageData() instead.
        // Return null; upload logic is handled by ImageServiceStrategy.
        return null;
    }

    @Override
    public ImageData getImageData(ImageRequest request) {
        String requirement = request.getEffectiveParam(true);
        return generateSvgDiagramData(requirement);
    }

    /**
     * Generate SVG conceptual diagram data
     *
     * @param requirement diagram requirement description
     * @return ImageData containing SVG bytes, or null on failure
     */
    public ImageData generateSvgDiagramData(String requirement) {
        if (StrUtil.isBlank(requirement)) {
            log.warn("SVG diagram requirement is empty");
            return null;
        }

        try {
            // 1. Call LLM to generate SVG code
            String svgCode = callLlmToGenerateSvg(requirement);

            if (StrUtil.isBlank(svgCode)) {
                log.error("LLM did not generate SVG code");
                return null;
            }

            // 2. Validate SVG format
            if (!isValidSvg(svgCode)) {
                log.error("Generated SVG code is invalid");
                return null;
            }

            // 3. Convert to byte data
            byte[] svgBytes = svgCode.getBytes(StandardCharsets.UTF_8);

            log.info("SVG diagram generated, size={} bytes", svgBytes.length);
            return ImageData.fromBytes(svgBytes, "image/svg+xml");

        } catch (Exception e) {
            log.error("SVG diagram generation failed, requirement={}", requirement, e);
            return null;
        }
    }

    /**
     * Call LLM to generate SVG code
     */
    private String callLlmToGenerateSvg(String requirement) {
        String prompt = PromptConstant.SVG_DIAGRAM_GENERATION_PROMPT
                .replace("{requirement}", requirement);

        log.info("Start calling LLM to generate SVG diagram");

        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        String svgCode = response.getResult().getOutput().getText().trim();

        // Extract SVG code (strip potential Markdown code block markers)
        svgCode = extractSvgCode(svgCode);

        return svgCode;
    }

    /**
     * Extract SVG code (strip Markdown code block markers)
     */
    private String extractSvgCode(String text) {
        if (text == null) {
            return null;
        }

        // Strip Markdown code block markers
        text = text.replace("```xml", "").replace("```svg", "").replace("```", "").trim();

        // Ensure XML declaration is present
        if (!text.startsWith("<?xml")) {
            // If there is no XML declaration but there is an <svg> tag, prepend the declaration
            if (text.contains("<svg")) {
                text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + text;
            }
        }

        return text;
    }

    /**
     * Validate SVG format
     */
    private boolean isValidSvg(String svgCode) {
        if (StrUtil.isBlank(svgCode)) {
            return false;
        }

        // Basic validation: must contain svg tags
        return svgCode.contains("<svg") && svgCode.contains("</svg>");
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.SVG_DIAGRAM;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }
}
