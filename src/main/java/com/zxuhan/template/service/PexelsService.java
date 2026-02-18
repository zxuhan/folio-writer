package com.zxuhan.template.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zxuhan.template.config.PexelsConfig;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;

import static com.zxuhan.template.constant.ArticleConstant.*;

/**
 * Pexels image search service
 */
@Service
@Slf4j
public class PexelsService implements ImageSearchService {

    @Resource
    private PexelsConfig pexelsConfig;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    public String searchImage(String keywords) {
        try {
            String url = buildSearchUrl(keywords);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", pexelsConfig.getApiKey())
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Pexels API call failed: {}", response.code());
                    return null;
                }

                String responseBody = response.body().string();
                return extractImageUrl(responseBody, keywords);
            }
        } catch (IOException e) {
            log.error("Pexels API call exception", e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * Build search URL
     *
     * @param keywords search keywords
     * @return full search URL
     */
    private String buildSearchUrl(String keywords) {
        return String.format("%s?query=%s&per_page=%d&orientation=%s",
                PEXELS_API_URL,
                keywords,
                PEXELS_PER_PAGE,
                PEXELS_ORIENTATION_LANDSCAPE);
    }

    /**
     * Extract image URL from response
     *
     * @param responseBody response body
     * @param keywords     search keywords (for logging)
     * @return image URL, or null if not found
     */
    private String extractImageUrl(String responseBody, String keywords) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray photos = jsonObject.getAsJsonArray("photos");

        if (photos.isEmpty()) {
            log.warn("No images found on Pexels: {}", keywords);
            return null;
        }

        JsonObject photo = photos.get(0).getAsJsonObject();
        JsonObject src = photo.getAsJsonObject("src");
        return src.get("large").getAsString();
    }
}
