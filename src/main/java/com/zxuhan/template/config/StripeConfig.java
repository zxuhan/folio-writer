package com.zxuhan.template.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Stripe payment configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeConfig {

    /**
     * Stripe API key.
     */
    private String apiKey;

    /**
     * Webhook signing secret.
     */
    private String webhookSecret;

    /**
     * Payment success callback URL.
     */
    private String successUrl;

    /**
     * Payment cancellation callback URL.
     */
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = this.apiKey;
    }
}
