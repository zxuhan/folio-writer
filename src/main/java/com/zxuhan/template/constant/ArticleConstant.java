package com.zxuhan.template.constant;

/**
 * Article-related constants.
 */
public interface ArticleConstant {

    /**
     * SSE connection timeout in milliseconds: 30 minutes.
     */
    long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /**
     * SSE reconnect delay in milliseconds: 3 seconds.
     */
    long SSE_RECONNECT_TIME_MS = 3000L;

    // region Pexels constants

    /**
     * Pexels API URL.
     */
    String PEXELS_API_URL = "https://api.pexels.com/v1/search";

    /**
     * Number of Pexels results per page.
     */
    int PEXELS_PER_PAGE = 1;

    /**
     * Pexels image orientation: landscape.
     */
    String PEXELS_ORIENTATION_LANDSCAPE = "landscape";

    // endregion

    // region Picsum constants

    /**
     * Picsum random image URL template.
     */
    String PICSUM_URL_TEMPLATE = "https://picsum.photos/800/600?random=%d";

    // endregion

    // region Bing meme constants

    /**
     * Bing image search URL.
     */
    String BING_IMAGE_SEARCH_URL = "https://www.bing.com/images/async";

    /**
     * Meme keyword suffix (appended programmatically).
     */
    String EMOJI_PACK_SUFFIX = "funny meme";

    /**
     * Maximum number of images per Bing search batch.
     */
    int BING_MAX_IMAGES = 30;

    // endregion

    // region SVG diagram constants

    /**
     * SVG file prefix.
     */
    String SVG_FILE_PREFIX = "svg-chart";

    /**
     * SVG default width.
     */
    int SVG_DEFAULT_WIDTH = 800;

    /**
     * SVG default height.
     */
    int SVG_DEFAULT_HEIGHT = 600;

    // endregion
}
