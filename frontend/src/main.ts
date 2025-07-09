import { createApp } from 'vue';
import App from './App.vue';
import router from '@/router'; // This should match the alias set in vite.config.ts
import PrimeVue from 'primevue/config';
import Aura from '@primevue/themes/aura';
import 'primeicons/primeicons.css'
import ToastService from 'primevue/toastservice';
import Tooltip from "primevue/tooltip";
import ConfirmationService from 'primevue/confirmationservice';

const app = createApp(App);

app.use(PrimeVue, {
  theme: {
    preset: Aura,
    options: {
      darkModeSelector: '.my-app-dark', // This selector triggers dark mode
    },
  }
}); // Register PrimeVue plugin
app.use(router); // Register Vue Router
app.use(ToastService);
app.directive('tooltip', Tooltip);
app.use(ConfirmationService);
app.mount('#app');
