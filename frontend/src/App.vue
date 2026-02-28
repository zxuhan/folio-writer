<script setup lang="ts">
import { provide } from 'vue'
import BasicLayout from '@/layouts/BasicLayout.vue'
import enUS from 'ant-design-vue/es/locale/en_US'

provide('locale', enUS)
</script>

<template>
  <a-config-provider :locale="enUS">
    <BasicLayout />
  </a-config-provider>
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&family=Work+Sans:wght@300;400;500;600;700&display=swap');

:root {
  /* Brand — warm orange */
  --color-primary: #F97316;
  --color-primary-light: #FB923C;
  --color-primary-dark: #EA580C;
  --color-secondary: #1C1917;
  --color-secondary-light: #44403C;

  /* Functional */
  --color-cta: #F97316;
  --color-success: #16A34A;
  --color-warning: #EAB308;
  --color-error: #EF4444;
  --color-info: #3B82F6;

  /* Neutrals */
  --color-background: #FFFFFF;
  --color-background-secondary: #FAFAF9;
  --color-background-tertiary: #F5F5F4;
  --color-background-dark: #1C1917;
  --color-text: #1C1917;
  --color-text-secondary: #57534E;
  --color-text-muted: #A8A29E;
  --color-border: #E7E5E4;
  --color-border-light: #F5F5F4;

  /* Gradients */
  --gradient-primary: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  --gradient-hero: linear-gradient(180deg, #FFEDD5 0%, #FFFFFF 100%);
  --gradient-hero-dark: linear-gradient(180deg, #9A3412 0%, #7C2D12 100%);

  /* Solid backgrounds */
  --bg-primary: #F97316;
  --bg-primary-hover: #EA580C;
  --bg-dark: #1C1917;
  --bg-dark-hover: #292524;
  --bg-success: #16A34A;

  /* Shadows */
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --shadow-card: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-card-hover: 0 8px 24px rgba(249, 115, 22, 0.15);
  --shadow-brand: 0 4px 14px rgba(249, 115, 22, 0.25);

  /* Radius */
  --radius-sm: 6px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-2xl: 24px;
  --radius-full: 9999px;

  /* Transitions */
  --transition-fast: 150ms ease-out;
  --transition-normal: 200ms ease-out;
  --transition-slow: 300ms ease-out;

  /* Glass */
  --glass-bg: rgba(255, 255, 255, 0.9);
  --glass-bg-warm: rgba(255, 237, 213, 0.8);
  --glass-blur: blur(12px);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: 'Work Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  background-color: var(--color-background);
  color: var(--color-text);
  line-height: 1.6;
}

html {
  overflow-x: hidden;
}

h1, h2, h3, h4, h5, h6 {
  font-family: 'Outfit', sans-serif;
}

a {
  color: var(--color-text);
  text-decoration: none;
  transition: color var(--transition-fast);
}

a:hover {
  color: var(--color-primary);
}

.ant-btn {
  transition: all var(--transition-normal);
  font-weight: 500;
  border-radius: var(--radius-md);
}

.ant-btn-primary {
  background: var(--bg-primary);
  border-color: var(--bg-primary);
  box-shadow: var(--shadow-brand);
}

.ant-btn-primary:not(:disabled):hover {
  background: var(--bg-primary-hover);
  border-color: var(--bg-primary-hover);
  box-shadow: 0 6px 20px rgba(249, 115, 22, 0.35);
}

.ant-card {
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  transition: all var(--transition-normal);
}

.clickable,
[role="button"],
button,
.ant-btn,
.ant-card.hoverable {
  cursor: pointer;
}

::selection {
  background: rgba(249, 115, 22, 0.2);
  color: var(--color-text);
}

.ant-menu-horizontal > .ant-menu-item:hover::after,
.ant-menu-horizontal > .ant-menu-item-active::after,
.ant-menu-horizontal > .ant-menu-item-selected::after {
  border-bottom-color: var(--color-primary) !important;
}

.ant-menu-horizontal > .ant-menu-item:hover,
.ant-menu-horizontal > .ant-menu-item-active {
  color: var(--color-text) !important;
}

.ant-btn:disabled {
  background: var(--color-border) !important;
  color: var(--color-text-muted) !important;
  border-color: var(--color-border) !important;
  box-shadow: none !important;
}

.ant-btn-primary:disabled {
  background: var(--color-border) !important;
  color: var(--color-text-muted) !important;
}

.ant-steps .ant-steps-item-process .ant-steps-item-icon {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish .ant-steps-item-icon {
  border-color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish .ant-steps-item-icon > .ant-steps-icon {
  color: var(--color-primary);
}

.ant-steps .ant-steps-item-finish > .ant-steps-item-container > .ant-steps-item-tail::after {
  background-color: var(--color-primary);
}

.ant-input:focus,
.ant-input-focused,
.ant-input-affix-wrapper:focus,
.ant-input-affix-wrapper-focused {
  border-color: var(--color-primary) !important;
  box-shadow: 0 0 0 2px rgba(249, 115, 22, 0.1) !important;
}

.ant-input:hover {
  border-color: var(--color-primary-light);
}

.ant-input-textarea-show-count::after {
  color: var(--color-text-muted);
}

.ant-progress-bg {
  background: var(--gradient-primary);
}

.ant-spin-dot-item {
  background-color: var(--color-primary);
}
</style>
