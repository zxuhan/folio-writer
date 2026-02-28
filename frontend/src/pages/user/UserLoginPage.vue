<template>
  <div id="userLoginPage">
    <div class="auth-container">
      <div class="brand-section">
        <div class="brand-bg"></div>
        <div class="brand-content">
          <div class="brand-logo">
            <img src="@/assets/logo.png" alt="Logo" class="logo-img" />
          </div>
          <h1 class="brand-title">Folio — AI Article Studio</h1>
          <p class="brand-subtitle">Helping everyone write articles that go viral</p>
          <div class="brand-features">
            <div class="feature-item">
              <CheckCircleOutlined class="feature-check" />
              <span>Smart title and outline generation</span>
            </div>
            <div class="feature-item">
              <CheckCircleOutlined class="feature-check" />
              <span>Streaming high-quality content</span>
            </div>
            <div class="feature-item">
              <CheckCircleOutlined class="feature-check" />
              <span>Auto image generation and one-click export</span>
            </div>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="form-card">
          <h2 class="form-title">Welcome Back</h2>
          <p class="form-subtitle">Sign in to your account to keep writing</p>

          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" class="login-form">
            <a-form-item name="userAccount" :rules="[{ required: true, message: 'Please enter your account' }]">
              <a-input
                v-model:value="formState.userAccount"
                placeholder="Enter your account"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <UserOutlined class="input-icon" />
                </template>
              </a-input>
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
                { required: true, message: 'Please enter your password' },
                { min: 8, message: 'Password must be at least 8 characters' },
              ]"
            >
              <a-input-password
                v-model:value="formState.userPassword"
                placeholder="Enter your password"
                size="large"
                class="form-input"
              >
                <template #prefix>
                  <LockOutlined class="input-icon" />
                </template>
              </a-input-password>
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block class="submit-btn">
                Sign In
              </a-button>
            </a-form-item>
          </a-form>

          <div class="form-footer">
            <span class="footer-text">Don't have an account?</span>
            <RouterLink to="/user/register" class="register-link">Sign up</RouterLink>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * Submit form
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // Sign-in successful: persist login state to global store
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('Signed in successfully')
    router.push({
      path: '/',
      replace: true,
    })
  } else {
    message.error('Sign in failed: ' + res.data.message)
  }
}
</script>

<style scoped>
#userLoginPage {
  min-height: calc(100vh - 64px);
  background: var(--color-background-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-container {
  display: flex;
  width: 100%;
  max-width: 900px;
  min-height: 520px;
  background: white;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
}

.brand-section {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #F97316 0%, #EA580C 50%, #C2410C 100%);
}

.brand-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 60%);
  animation: pulse-bg 8s ease-in-out infinite;
}

@keyframes pulse-bg {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.3; }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-img {
  width: 80px;
  height: 80px;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-xl);
  padding: 8px;
}

.brand-title {
  font-size: 26px;
  font-weight: 700;
  margin: 0 0 10px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.9;
  margin: 0 0 36px;
}

.brand-features {
  text-align: left;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  padding: 20px 24px;
  backdrop-filter: blur(8px);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 14px;
}

.feature-item:last-child {
  margin-bottom: 0;
}

.feature-check {
  font-size: 18px;
  color: white;
}

.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
  background: white;
}

.form-card {
  width: 100%;
  max-width: 320px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 6px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 32px;
}

.login-form {
  margin-bottom: 24px;
}

.form-input {
  border-radius: var(--radius-lg);
  border-color: var(--color-border);
  transition: all var(--transition-fast);
}

.form-input:hover {
  border-color: var(--color-primary-light);
}

.form-input:focus,
.form-input:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.form-input :deep(.ant-input) {
  padding: 12px 14px;
}

.input-icon {
  color: var(--color-text-muted);
  font-size: 16px;
}

.submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  box-shadow: var(--shadow-brand) !important;
  transition: opacity var(--transition-normal) !important;
}

.submit-btn:hover,
.submit-btn:focus,
.submit-btn:active {
  background: var(--gradient-primary) !important;
  border: none !important;
  color: white !important;
  box-shadow: var(--shadow-brand) !important;
  opacity: 0.92;
}

.submit-btn :deep(.ant-wave) {
  display: none;
}

.form-footer {
  text-align: center;
}

.footer-text {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.register-link {
  color: var(--color-primary);
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--transition-fast);
}

.register-link:hover {
  color: var(--color-primary-dark);
}

@media (max-width: 768px) {
  .auth-container {
    flex-direction: column;
    min-height: auto;
    border-radius: var(--radius-xl);
  }

  .brand-section {
    padding: 32px 24px;
  }

  .brand-title {
    font-size: 22px;
  }

  .brand-features {
    display: none;
  }

  .form-section {
    padding: 32px 24px;
  }

  .form-title {
    font-size: 22px;
  }
}
</style>
