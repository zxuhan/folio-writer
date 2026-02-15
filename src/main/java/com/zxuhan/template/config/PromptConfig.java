package com.zxuhan.template.config;

import com.zxuhan.template.constant.PromptConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Prompt configuration class.
 * Supports overriding default prompts via configuration files.
 */
@Configuration
@ConfigurationProperties(prefix = "prompt")
@Data
public class PromptConfig {

    /**
     * Prompt version number.
     */
    private String version = "1.0";

    /**
     * Prompt template map.
     */
    private Map<String, String> templates = new HashMap<>();

    @PostConstruct
    public void init() {
        // Initialize defaults from PromptConstant
        templates.putIfAbsent("agent1_title", PromptConstant.AGENT1_TITLE_PROMPT);
        templates.putIfAbsent("agent2_outline", PromptConstant.AGENT2_OUTLINE_PROMPT);
        templates.putIfAbsent("agent3_content", PromptConstant.AGENT3_CONTENT_PROMPT);
        templates.putIfAbsent("agent4_image", PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT);
        templates.putIfAbsent("ai_modify_outline", PromptConstant.AI_MODIFY_OUTLINE_PROMPT);
    }

    /**
     * Get a prompt template by key.
     *
     * @param key the prompt key
     * @return the prompt content
     */
    public String getPrompt(String key) {
        return templates.getOrDefault(key, "");
    }
}
