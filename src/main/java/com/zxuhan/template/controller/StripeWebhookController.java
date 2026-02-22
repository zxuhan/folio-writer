package com.zxuhan.template.controller;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.zxuhan.template.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Stripe Webhook controller
 *
 */
@RestController
@RequestMapping("/webhook")
@Slf4j
@Hidden
public class StripeWebhookController {

    @Resource
    private PaymentService paymentService;

    /**
     * Handle Stripe Webhook callback
     */
    @PostMapping("/stripe")
    public String handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            // Verify webhook signature
            Event event = paymentService.constructEvent(payload, sigHeader);

            log.info("Received Stripe Webhook event, type={}", event.getType());

            // Handle event
            switch (event.getType()) {
                case "checkout.session.completed":
                case "checkout.session.async_payment_succeeded":
                    paymentService.handlePaymentSuccess(deserializeSession(event));
                    break;

                default:
                    log.info("Unhandled event type: {}", event.getType());
                    break;
            }

            return "success";
        } catch (Exception e) {
            log.error("Failed to handle Stripe Webhook", e);
            return "error";
        }
    }

    // Falls back to deserializeUnsafe() when the event's API version is newer
    // than the stripe-java SDK was compiled against — otherwise getObject()
    // silently returns Optional.empty().
    private Session deserializeSession(Event event) throws Exception {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject obj = deserializer.getObject().orElse(null);
        if (obj == null) {
            obj = deserializer.deserializeUnsafe();
        }
        return (Session) obj;
    }
}
