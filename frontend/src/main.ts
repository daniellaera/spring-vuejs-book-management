import { createApp } from 'vue';
import App from './App.vue';
import router from '@/router'; // This should match the alias set in vite.config.ts

createApp(App).use(router).mount('#app');
