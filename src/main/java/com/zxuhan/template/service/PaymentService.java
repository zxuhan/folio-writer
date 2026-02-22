package com.zxuhan.template.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.zxuhan.template.model.entity.PaymentRecord;

import java.util.List;

/**
 * Payment service
 */
public interface PaymentService {

    /**
     * Create a VIP lifetime membership payment session
     *
     * @param userId user ID
     * @return Stripe Checkout Session URL
     */
    String createVipPaymentSession(Long userId) throws StripeException;

    /**
     * Handle successful payment callback
     *
     * @param session Stripe Checkout Session
     */
    void handlePaymentSuccess(Session session);

    /**
     * Process refund
     *
     * @param userId user ID
     * @param reason refund reason
     * @return whether the refund succeeded
     */
    boolean handleRefund(Long userId, String reason) throws StripeException;

    /**
     * Verify webhook signature
     *
     * @param payload   request body
     * @param sigHeader signature header
     * @return Stripe Event
     */
    Event constructEvent(String payload, String sigHeader) throws Exception;

    /**
     * Get payment records for a user
     *
     * @param userId user ID
     * @return list of payment records
     */
    List<PaymentRecord> getPaymentRecords(Long userId);
}
