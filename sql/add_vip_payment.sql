-- Add VIP membership and payment support

use folio;

-- 1. Extend the user table with VIP-related columns
ALTER TABLE user
ADD COLUMN vipTime DATETIME NULL COMMENT 'VIP membership start time';

-- 2. Create the payment record table
CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    userId BIGINT NOT NULL COMMENT 'User ID',
    stripeSessionId VARCHAR(128) COMMENT 'Stripe Checkout Session ID',
    stripePaymentIntentId VARCHAR(128) COMMENT 'Stripe payment intent ID',
    amount DECIMAL(10,2) NOT NULL COMMENT 'Amount (USD)',
    currency VARCHAR(8) DEFAULT 'usd' COMMENT 'Currency',
    status VARCHAR(32) NOT NULL COMMENT 'Status: PENDING/SUCCEEDED/FAILED/REFUNDED',
    productType VARCHAR(32) NOT NULL COMMENT 'Product type: VIP_PERMANENT',
    description VARCHAR(256) COMMENT 'Description',
    refundTime DATETIME NULL COMMENT 'Refund time',
    refundReason VARCHAR(512) NULL COMMENT 'Refund reason',
    createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',

    INDEX idx_userId (userId),
    INDEX idx_stripeSessionId (stripeSessionId),
    INDEX idx_status (status),
    INDEX idx_createTime (createTime)
) COMMENT 'Payment records' COLLATE = utf8mb4_unicode_ci;
