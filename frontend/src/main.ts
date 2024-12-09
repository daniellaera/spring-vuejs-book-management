import { createApp } from 'vue';
import App from './App.vue';
import router from '@/router'; // This should match the alias set in vite.config.ts
import PrimeVue from 'primevue/config';
import Aura from '@primevue/themes/aura';
import 'primeicons/primeicons.css'
import ToastService from 'primevue/toastservice';

const app = createApp(App);

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.my-app-dark',  // This selector triggers dark mode
    },
  }
}); // Register PrimeVue plugin
app.use(router);   // Register Vue Router
app.use(ToastService);

app.mount('#app');
