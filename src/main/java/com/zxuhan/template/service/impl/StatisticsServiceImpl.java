package com.zxuhan.template.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.zxuhan.template.constant.UserConstant;
import com.zxuhan.template.mapper.ArticleMapper;
import com.zxuhan.template.mapper.UserMapper;
import com.zxuhan.template.model.entity.Article;
import com.zxuhan.template.model.entity.User;
import com.zxuhan.template.model.enums.ArticleStatusEnum;
import com.zxuhan.template.model.vo.StatisticsVO;
import com.zxuhan.template.service.AgentLogService;
import com.zxuhan.template.service.StatisticsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Statistics service implementation
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private static final String STATISTICS_CACHE_KEY = "statistics:overview";
    private static final long CACHE_EXPIRE_HOURS = 1L;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AgentLogService agentLogService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public StatisticsVO getStatistics() {
        // Try cache first
        StatisticsVO cachedStats = (StatisticsVO) redisTemplate.opsForValue().get(STATISTICS_CACHE_KEY);
        if (cachedStats != null) {
            log.info("Statistics loaded from cache");
            return cachedStats;
        }

        // Cache miss: recalculate
        // Articles created today
        Long todayCount = countArticlesByDateRange(getTodayStart(), LocalDateTime.now());

        // Articles created this week
        Long weekCount = countArticlesByDateRange(getWeekStart(), LocalDateTime.now());

        // Articles created this month
        Long monthCount = countArticlesByDateRange(getMonthStart(), LocalDateTime.now());

        // Total articles
        Long totalCount = countTotalArticles();

        // Success rate
        Double successRate = calculateSuccessRate();

        // Average duration
        Integer avgDurationMs = calculateAvgDuration();

        // Active users this week (users who created at least one article)
        Long activeUserCount = countActiveUsers(getWeekStart());

        // Total users
        Long totalUserCount = countTotalUsers();

        // VIP users
        Long vipUserCount = countVipUsers();

        // Quota usage (total quota - remaining quota)
        Long quotaUsed = calculateQuotaUsed();

        StatisticsVO statistics = StatisticsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .successRate(successRate)
                .avgDurationMs(avgDurationMs)
                .activeUserCount(activeUserCount)
                .totalUserCount(totalUserCount)
                .vipUserCount(vipUserCount)
                .quotaUsed(quotaUsed)
                .build();

        // Store in cache, expire after 1 hour
        redisTemplate.opsForValue().set(STATISTICS_CACHE_KEY, statistics, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("Statistics cached, expiry: {} hour(s)", CACHE_EXPIRE_HOURS);

        return statistics;
    }

    /**
     * Count articles within a given date range
     */
    private Long countArticlesByDateRange(LocalDateTime start, LocalDateTime end) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("createTime", start)
                .le("createTime", end);
        return articleMapper.selectCountByQuery(queryWrapper);
    }

    /**
     * Count total articles
     */
    private Long countTotalArticles() {
        return articleMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * Calculate success rate
     */
    private Double calculateSuccessRate() {
        Long totalCount = countTotalArticles();
        if (totalCount == 0) {
            return 0.0;
        }

        QueryWrapper successWrapper = QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue());
        Long successCount = articleMapper.selectCountByQuery(successWrapper);

        return (successCount.doubleValue() / totalCount.doubleValue()) * 100;
    }

    /**
     * Calculate average duration (from creation to completion)
     */
    private Integer calculateAvgDuration() {
        // Query all completed articles and calculate average time from createTime to completedTime
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue())
                .isNotNull("completedTime");

        try {
            List<Article> completedArticles = articleMapper.selectListByQuery(queryWrapper);
            if (completedArticles == null || completedArticles.isEmpty()) {
                return 0;
            }

            // Calculate duration in milliseconds for each article
            double avgDuration = completedArticles.stream()
                    .filter(article -> article.getCreateTime() != null && article.getCompletedTime() != null)
                    .mapToLong(article -> {
                        long createMillis = java.sql.Timestamp.valueOf(article.getCreateTime()).getTime();
                        long completedMillis = java.sql.Timestamp.valueOf(article.getCompletedTime()).getTime();
                        return completedMillis - createMillis;
                    })
                    .average()
                    .orElse(0.0);

            return (int) avgDuration;
        } catch (Exception e) {
            log.warn("Average duration calculation failed", e);
        }

        return 0;
    }

    /**
     * Count active users this week (users who created at least one article)
     */
    private Long countActiveUsers(LocalDateTime weekStart) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("createTime", weekStart);

        try {
            List<Article> articles = articleMapper.selectListByQuery(queryWrapper);
            // Count distinct users
            return articles.stream()
                    .map(Article::getUserId)
                    .distinct()
                    .count();
        } catch (Exception e) {
            log.warn("Active user count failed", e);
        }

        return 0L;
    }

    /**
     * Count total users
     */
    private Long countTotalUsers() {
        return userMapper.selectCountByQuery(QueryWrapper.create());
    }

    /**
     * Count VIP users
     */
    private Long countVipUsers() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.VIP_ROLE);
        return userMapper.selectCountByQuery(queryWrapper);
    }

    /**
     * Calculate quota usage
     */
    private Long calculateQuotaUsed() {
        // Quota used = (regular user count * initial quota) - sum of remaining quotas
        QueryWrapper normalUserWrapper = QueryWrapper.create()
                .eq("userRole", UserConstant.DEFAULT_ROLE);

        try {
            List<User> normalUsers = userMapper.selectListByQuery(normalUserWrapper);
            Long normalUserCount = (long) normalUsers.size();

            // Sum remaining quotas
            long remainingQuota = normalUsers.stream()
                    .mapToInt(user -> user.getQuota() != null ? user.getQuota() : 0)
                    .sum();

            return (normalUserCount * UserConstant.DEFAULT_QUOTA) - remainingQuota;
        } catch (Exception e) {
            log.warn("Quota usage calculation failed", e);
        }

        return 0L;
    }

    /**
     * Get start of today
     */
    private LocalDateTime getTodayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    /**
     * Get start of the current week (Monday)
     */
    private LocalDateTime getWeekStart() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        return LocalDateTime.of(monday, LocalTime.MIN);
    }

    /**
     * Get start of the current month
     */
    private LocalDateTime getMonthStart() {
        LocalDate today = LocalDate.now();
        LocalDate firstDay = today.withDayOfMonth(1);
        return LocalDateTime.of(firstDay, LocalTime.MIN);
    }
}
