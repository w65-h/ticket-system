import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import {
  DataAnalysis, Tickets, Setting,
  Fold, Expand,
  UserFilled, ArrowDown
} from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

const icons = { DataAnalysis, Tickets, Setting, Fold, Expand, UserFilled, ArrowDown }
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')