import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'
import enUS from 'ant-design-vue/es/locale/en_US'
import 'ant-design-vue/dist/reset.css'
import 'dayjs/locale/en'

import '@/access'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.provide('locale', enUS)

app.mount('#app')
