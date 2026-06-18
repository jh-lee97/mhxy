import { createApp } from 'vue'
import './styles/global.css'
import App from './views/App.vue'
import router from './router'

createApp(App).use(router).mount('#app')
