/**
 * Environment config example.
 *
 * The real `env.ts` reads values from Vite env files
 * (`.env.development`, `.env.production`). Edit those — not this file.
 */

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || ''
