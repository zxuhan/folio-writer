package com.zxuhan.template.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zxuhan.template.config.IconifyConfig;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.zxuhan.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Iconify icon library search service.
 * Provides search across 275k+ open-source icons and SVG generation.
 */
@Service
@Slf4j
public class IconifyService implements ImageSearchService {

    @Resource
    private IconifyConfig iconifyConfig;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public String searchImage(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            log.warn("Iconify search keywords are empty");
            return null;
        }

        try {
            // 1. Search for icon
            String searchUrl = buildSearchUrl(keywords);
            String searchResult = callApi(searchUrl);

            if (searchResult == null) {
                return null;
            }

            // 2. Parse result, get first icon
            String iconName = extractFirstIcon(searchResult);
            if (iconName == null) {
                log.warn("No Iconify icon found: {}", keywords);
                return null;
            }

            // 3. Build SVG URL
            String svgUrl = buildSvgUrl(iconName);
            log.info("Iconify icon found: {} -> {}", keywords, iconName);
            
            return svgUrl;

        } catch (Exception e) {
            log.error("Iconify icon search failed, keywords={}", keywords, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.ICONIFY;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * Build search URL
     */
    private String buildSearchUrl(String keywords) {
        String encodedKeywords = URLEncoder.encode(keywords, StandardCharsets.UTF_8);
        return String.format("%s/search?query=%s&limit=%d",
                iconifyConfig.getApiUrl(),
                encodedKeywords,
                iconifyConfig.getSearchLimit());
    }

    /**
     * Call Iconify API
     */
    private String callApi(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Iconify API call failed: {}", response.code());
                    return null;
                }

                return response.body().string();
            }
        } catch (IOException e) {
            log.error("Iconify API call exception", e);
            return null;
        }
    }

    /**
     * Extract the first icon name from search results
     */
    private String extractFirstIcon(String jsonResponse) {
        try {
            JsonObject json = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray icons = json.getAsJsonArray("icons");

            if (icons == null || icons.isEmpty()) {
                return null;
            }

            return icons.get(0).getAsString();
        } catch (Exception e) {
            log.error("Failed to parse Iconify search result", e);
            return null;
        }
    }

    /**
     * Build SVG URL
     *
     * @param iconName icon name (format: prefix:name, e.g. mdi:home)
     * @return SVG URL
     */
    private String buildSvgUrl(String iconName) {
        // Convert "mdi:home" to "mdi/home"
        String path = iconName.replace(":", "/");

        StringBuilder url = new StringBuilder(iconifyConfig.getApiUrl())
                .append("/")
                .append(path)
                .append(".svg");

        // Add height parameter
        boolean hasParams = false;
        if (iconifyConfig.getDefaultHeight() != null && iconifyConfig.getDefaultHeight() > 0) {
            url.append("?height=").append(iconifyConfig.getDefaultHeight());
            hasParams = true;
        }

        // Add color parameter (if configured)
        if (iconifyConfig.getDefaultColor() != null && !iconifyConfig.getDefaultColor().isEmpty()) {
            url.append(hasParams ? "&" : "?");

            // Handle color format (#000000 → %23000000)
            String color = iconifyConfig.getDefaultColor();
            if (color.startsWith("#")) {
                color = "%23" + color.substring(1);
            }
            url.append("color=").append(color);
        }

        return url.toString();
    }
}
