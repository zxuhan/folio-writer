/**
 * Environment-driven config.
 * Values come from `.env.development` / `.env.production` (Vite env files).
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || ''
