package com.zxuhan.template.service;

import cn.hutool.core.util.StrUtil;
import com.zxuhan.template.config.EmojiPackConfig;
import com.zxuhan.template.model.enums.ImageMethodEnum;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.zxuhan.template.constant.ArticleConstant.PICSUM_URL_TEMPLATE;

/**
 * Meme image search service (powered by Bing Image Search).
 * Automatically appends a meme search suffix to the provided keywords.
 */
@Service
@Slf4j
public class EmojiPackService implements ImageSearchService {

    @Resource
    private EmojiPackConfig emojiPackConfig;

    @Override
    public String searchImage(String keywords) {
        if (StrUtil.isBlank(keywords)) {
            log.warn("Meme search keywords are empty");
            return null;
        }

        try {
            // 1. Build search term (append meme suffix)
            String searchText = keywords + emojiPackConfig.getSuffix();
            log.info("Meme search: {} -> {}", keywords, searchText);

            // 2. Build search URL
            String fetchUrl = buildSearchUrl(searchText);

            // 3. Fetch page with Jsoup
            Document document = Jsoup.connect(fetchUrl)
                    .timeout(emojiPackConfig.getTimeout())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // 4. Locate image container
            Element div = document.getElementsByClass("dgControl").first();
            if (div == null) {
                log.warn("Bing image container not found, keywords={}", keywords);
                return null;
            }

            // 5. Extract images using CSS selector
            Elements imgElements = div.select("img.mimg");
            if (imgElements.isEmpty()) {
                log.warn("No meme images found on Bing, keywords={}, searchText={}", keywords, searchText);
                return null;
            }

            // 6. Get first image URL
            String imageUrl = imgElements.get(0).attr("src");
            if (StrUtil.isBlank(imageUrl)) {
                log.warn("Image URL is empty, keywords={}", keywords);
                return null;
            }

            // 7. Clean URL parameters (remove ?w=xxx&h=xxx)
            imageUrl = cleanImageUrl(imageUrl);

            log.info("Meme image found: {} -> {}", keywords, imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("Meme search failed, keywords={}", keywords, e);
            return null;
        }
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.EMOJI_PACK;
    }

    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * Build Bing image search URL
     *
     * @param searchText search text
     * @return full search URL
     */
    private String buildSearchUrl(String searchText) {
        String encodedText = URLEncoder.encode(searchText, StandardCharsets.UTF_8);
        // mmasync=1 parameter is required
        return String.format("%s?q=%s&mmasync=1",
                emojiPackConfig.getSearchUrl(),
                encodedText);
    }

    /**
     * Clean image URL parameters.
     * Removes ?w=xxx&h=xxx etc. to avoid image quality degradation and access issues.
     *
     * @param url raw image URL
     * @return cleaned URL
     */
    private String cleanImageUrl(String url) {
        if (StrUtil.isBlank(url)) {
            return url;
        }
        
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex > 0) {
            return url.substring(0, questionMarkIndex);
        }
        
        return url;
    }
}
