<template>
  <div class="vip-page">
    <div class="vip-container">
      <div class="page-header">
        <div class="header-badge">
          <CrownOutlined />
          <span>Members Only</span>
        </div>
        <h1 class="page-title">Upgrade to Lifetime Membership</h1>
        <p class="page-subtitle">Unlock all premium features, unlimited creation quota, valid for life</p>
      </div>

      <div class="main-section">
        <div class="pricing-card">
          <div class="pricing-badge">Limited-time offer</div>
          <div class="pricing-header">
            <div class="plan-icon">
              <CrownOutlined />
            </div>
            <h2 class="plan-name">Lifetime Membership</h2>
            <div class="price-display">
              <span class="currency">$</span>
              <span class="price">199</span>
              <span class="period">/lifetime</span>
            </div>
            <div class="original-price">
              <span class="original-label">Originally</span>
              <span class="original-value">$299</span>
            </div>
          </div>

          <div class="pricing-divider"></div>

          <div class="pricing-features">
            <div v-for="(item, index) in pricingFeatures" :key="index" class="pricing-feature">
              <CheckCircleOutlined class="feature-check" />
              <span>{{ item }}</span>
            </div>
          </div>

          <a-button
            type="primary"
            size="large"
            :loading="purchasing"
            :disabled="isVip"
            @click="handlePurchase"
            class="purchase-btn"
          >
            <template #icon>
              <ThunderboltOutlined />
            </template>
            {{ isVip ? 'You are already a lifetime member' : 'Upgrade Now' }}
          </a-button>

          <div class="security-notice">
            <SafetyOutlined />
            <span>Secure payment · 7-day refund, no questions asked</span>
          </div>
        </div>

        <div class="features-section">
          <h3 class="features-title">
            <GiftOutlined />
            Member Benefits
          </h3>
          <div class="features-grid">
            <div v-for="(feature, index) in features" :key="index" class="feature-card">
              <div class="feature-icon-wrapper">
                <component :is="feature.icon" class="feature-icon" />
              </div>
              <div class="feature-content">
                <h4 class="feature-title">{{ feature.title }}</h4>
                <p class="feature-desc">{{ feature.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="faq-section">
        <div class="section-header">
          <QuestionCircleOutlined class="section-icon" />
          <h2 class="section-title">FAQ</h2>
        </div>
        <div class="faq-grid">
          <div v-for="(faq, index) in faqs" :key="index" class="faq-card">
            <h4 class="faq-question">{{ faq.question }}</h4>
            <p class="faq-answer">{{ faq.answer }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  CheckCircleOutlined,
  CrownOutlined,
  SafetyOutlined,
  ThunderboltOutlined,
  RocketOutlined,
  PictureOutlined,
  AppstoreOutlined,
  EditOutlined,
  StarOutlined,
  GiftOutlined,
  QuestionCircleOutlined
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { createVipPaymentSession } from '@/api/paymentController'
import { isVip as checkIsVip } from '@/utils/permission'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const purchasing = ref(false)

// Admin users are also treated as VIP
const isVip = computed(() => checkIsVip(loginUserStore.loginUser))

const features = [
  {
    icon: RocketOutlined,
    title: 'Unlimited Creation Quota',
    desc: 'Use the article creation feature without any quota limits'
  },
  {
    icon: PictureOutlined,
    title: 'AI Image Generation',
    desc: 'Generate unique illustrations with Nano Banana AI'
  },
  {
    icon: AppstoreOutlined,
    title: 'SVG Diagram Generation',
    desc: 'Auto-generate beautiful concept diagrams and mind maps'
  },
  {
    icon: EditOutlined,
    title: 'AI Outline Editor',
    desc: 'Polish your outline quickly with the AI assistant'
  },
  {
    icon: StarOutlined,
    title: 'Priority Queue',
    desc: 'Enjoy faster generation speeds and priority service'
  },
  {
    icon: GiftOutlined,
    title: 'Lifetime Validity',
    desc: 'One-time purchase, lifetime use, no renewals required'
  }
]

const pricingFeatures = [
  'Unlimited creation quota',
  'All premium image features',
  'AI outline editing',
  'Priority generation queue',
  'Lifetime validity'
]

const faqs = [
  {
    question: 'How quickly does it take effect after payment?',
    answer: 'Your lifetime membership is granted immediately after a successful payment. Refresh the page to see the change.'
  },
  {
    question: 'How do I request a refund?',
    answer: 'Within 7 days of purchase, you can request a refund if you are not satisfied. Membership privileges will be revoked after the refund.'
  },
  {
    question: 'Does the membership require renewal?',
    answer: 'No. Lifetime membership is a one-time purchase, valid for life, with no renewal required.'
  },
  {
    question: 'Is the payment secure?',
    answer: 'We use Stripe for international payments, with end-to-end encryption that is safe and reliable.'
  }
]

// Stripe redirects the browser back faster than the webhook can land,
// so poll the user a few times before deciding the upgrade actually went through.
const VIP_POLL_ATTEMPTS = 10
const VIP_POLL_INTERVAL_MS = 1000

const waitForVipUpgrade = async (): Promise<boolean> => {
  for (let i = 0; i < VIP_POLL_ATTEMPTS; i++) {
    await loginUserStore.fetchLoginUser()
    if (checkIsVip(loginUserStore.loginUser)) {
      return true
    }
    await new Promise((resolve) => setTimeout(resolve, VIP_POLL_INTERVAL_MS))
  }
  return false
}

onMounted(async () => {
  const success = route.query.success
  const cancelled = route.query.cancelled

  if (success === 'true') {
    router.replace('/vip')
    const loading = message.loading('Confirming payment, please wait...', 0)
    const upgraded = await waitForVipUpgrade()
    loading()

    if (upgraded) {
      Modal.success({
        title: 'Payment successful!',
        content: 'Congratulations on becoming a lifetime member. All premium features are now unlocked.',
        okText: 'Start Writing',
        onOk: () => {
          router.push('/create')
        }
      })
    } else {
      Modal.info({
        title: 'Payment received',
        content:
          'Your payment has been received but the upgrade is still processing. ' +
          'Please refresh the page in a minute. If it still does not show, contact support.',
        okText: 'OK'
      })
    }
  } else if (cancelled === 'true') {
    message.info('Payment cancelled')
    router.replace('/vip')
  }
})

const handlePurchase = async () => {
  if (!loginUserStore.loginUser.id) {
    message.warning('Please sign in first')
    router.push('/user/login')
    return
  }

  if (isVip.value) {
    message.info('You are already a lifetime member')
    return
  }

  purchasing.value = true
  try {
    const res = await createVipPaymentSession()
    if (res.data.code === 0 && res.data.data) {
      window.location.href = res.data.data
    } else {
      message.error(res.data.message || 'Failed to create payment session')
    }
  } catch (error) {
    console.error('Failed to create payment session:', error)
    message.error('Failed to create payment session, please try again later')
  } finally {
    purchasing.value = false
  }
}
</script>

<style scoped lang="scss">
.vip-page {
  min-height: calc(100vh - 64px);
  background: var(--gradient-hero);
  padding: 48px 24px 80px;
}

.vip-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(249, 115, 22, 0.1);
  border: 1px solid rgba(249, 115, 22, 0.2);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-dark);
  margin-bottom: 20px;

  .anticon {
    font-size: 14px;
  }
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px;
  color: var(--color-text);
  letter-spacing: -0.5px;
}

.page-subtitle {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

.main-section {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 32px;
  margin-bottom: 56px;
}

.pricing-card {
  background: white;
  border-radius: var(--radius-xl);
  padding: 36px 32px;
  box-shadow: var(--shadow-xl);
  border: 2px solid var(--color-primary);
  position: relative;
  height: fit-content;
  position: sticky;
  top: 88px;
}

.pricing-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--gradient-primary);
  color: white;
  padding: 6px 20px;
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  box-shadow: var(--shadow-brand);
}

.pricing-header {
  text-align: center;
  padding-bottom: 20px;
}

.plan-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(249, 115, 22, 0.1);
  border-radius: var(--radius-lg);

  .anticon {
    font-size: 26px;
    color: var(--color-primary);
  }
}

.plan-name {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 14px;
  color: var(--color-text);
}

.price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 6px;
}

.currency {
  font-size: 18px;
  color: var(--color-text-secondary);
  margin-right: 2px;
  font-weight: 500;
}

.price {
  font-size: 52px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.period {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-left: 4px;
}

.original-price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
}

.original-label {
  color: var(--color-text-muted);
}

.original-value {
  color: var(--color-text-muted);
  text-decoration: line-through;
}

.pricing-divider {
  height: 1px;
  background: var(--color-border-light);
  margin: 20px 0;
}

.pricing-features {
  margin-bottom: 24px;
}

.pricing-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  font-size: 14px;
  color: var(--color-text);

  .feature-check {
    color: var(--color-primary);
    font-size: 15px;
    flex-shrink: 0;
  }
}

.purchase-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  background: var(--gradient-primary) !important;
  border: none !important;
  box-shadow: var(--shadow-brand) !important;
  border-radius: var(--radius-md) !important;

  &:hover:not(:disabled) {
    opacity: 0.9;
    transform: translateY(-1px);
  }

  &:disabled {
    background: var(--color-background-tertiary) !important;
    color: var(--color-text-secondary) !important;
    box-shadow: none !important;
  }
}

.security-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 14px;
  font-size: 12px;
  color: var(--color-text-secondary);

  .anticon {
    color: var(--color-primary);
    font-size: 13px;
  }
}

.features-section {
  background: white;
  border-radius: var(--radius-xl);
  padding: 32px;
  border: 1px solid var(--color-border);
}

.features-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 24px;
  color: var(--color-text);

  .anticon {
    color: var(--color-primary);
    font-size: 20px;
  }
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 20px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);

  &:hover {
    background: rgba(249, 115, 22, 0.06);
  }
}

.feature-icon-wrapper {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(249, 115, 22, 0.1);
  border-radius: var(--radius-md);
}

.feature-icon {
  font-size: 18px;
  color: var(--color-primary);
}

.feature-content {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--color-text);
}

.feature-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.5;
}

.faq-section {
  background: white;
  border-radius: var(--radius-xl);
  padding: 32px;
  border: 1px solid var(--color-border);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.section-icon {
  font-size: 20px;
  color: var(--color-primary);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
}

.faq-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.faq-card {
  padding: 20px;
  background: var(--color-background-secondary);
  border-radius: var(--radius-lg);
}

.faq-question {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--color-text);
}

.faq-answer {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin: 0;
  line-height: 1.6;
}

@media (max-width: 992px) {
  .main-section {
    grid-template-columns: 1fr;
  }

  .pricing-card {
    position: static;
    max-width: 400px;
    margin: 0 auto;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .faq-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .vip-page {
    padding: 32px 16px 60px;
  }

  .page-title {
    font-size: 28px;
  }

  .page-subtitle {
    font-size: 14px;
  }

  .pricing-card {
    padding: 28px 24px;
  }

  .price {
    font-size: 44px;
  }

  .features-section,
  .faq-section {
    padding: 24px;
  }
}
</style>
