package com.zxuhan.template.model.vo;

import com.google.gson.reflect.TypeToken;
import com.zxuhan.template.model.entity.Article;
import com.zxuhan.template.utils.GsonUtils;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Article view object
 */
@Data
public class ArticleVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * Task ID
     */
    private String taskId;

    /**
     * User ID
     */
    private Long userId;

    /**
     * Article topic
     */
    private String topic;

    /**
     * Supplementary description from the user
     */
    private String userDescription;

    /**
     * Main title
     */
    private String mainTitle;

    /**
     * Subtitle
     */
    private String subTitle;

    /**
     * Title options list
     */
    private List<TitleOption> titleOptions;

    /**
     * Outline
     */
    private List<OutlineItem> outline;

    /**
     * Article content
     */
    private String content;

    /**
     * Full article content (with images)
     */
    private String fullContent;

    /**
     * Cover image URL
     */
    private String coverImage;

    /**
     * Image list
     */
    private List<ImageItem> images;

    /**
     * Status
     */
    private String status;

    /**
     * Current phase
     */
    private String phase;

    /**
     * Error message
     */
    private String errorMessage;

    /**
     * Creation timestamp
     */
    private LocalDateTime createTime;

    /**
     * Completion timestamp
     */
    private LocalDateTime completedTime;

    /**
     * Title option
     */
    @Data
    public static class TitleOption implements Serializable {
        private String mainTitle;
        private String subTitle;
    }

    /**
     * Outline item
     */
    @Data
    public static class OutlineItem implements Serializable {
        private Integer section;
        private String title;
        private List<String> points;
    }

    /**
     * Image item
     */
    @Data
    public static class ImageItem implements Serializable {
        private Integer position;
        private String url;
        private String method;
        private String keywords;
        private String sectionTitle;
        private String description;
    }

    /**
     * Converts an Article entity to ArticleVO.
     *
     * @param article article entity
     * @return ArticleVO instance
     */
    public static ArticleVO objToVo(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);

        // Convert JSON fields
        if (article.getTitleOptions() != null) {
            articleVO.setTitleOptions(GsonUtils.fromJson(article.getTitleOptions(), 
                new TypeToken<List<TitleOption>>(){}));
        }
        if (article.getOutline() != null) {
            articleVO.setOutline(GsonUtils.fromJson(article.getOutline(), 
                new TypeToken<List<OutlineItem>>(){}));
        }
        if (article.getImages() != null) {
            articleVO.setImages(GsonUtils.fromJson(article.getImages(), 
                new TypeToken<List<ImageItem>>(){}));
        }
        
        return articleVO;
    }

    private static final long serialVersionUID = 1L;
}
