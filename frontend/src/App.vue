<template>
  <NavBar @toggle-night-mode="toggleNightMode" />
  <!-- RouterView to render the current view like HomeView -->
  <RouterView />
</template>

<script setup lang="ts">
import NavBar from '@/views/NavBar.vue';
import { useNightMode } from '@/service/useNightMode'; // Corrected the import path

// Accessing night mode state and toggle function from the composable
const { isNightMode, toggleNightMode } = useNightMode();

// Watch for changes in the night mode state and toggle the body class
import {onMounted, watchEffect} from 'vue';
import {checkSession} from "@/service/useSession";

watchEffect(() => {
  if (isNightMode.value) {
    document.body.classList.add('night-mode');
  } else {
    document.body.classList.remove('night-mode');
  }
});
// Call checkSession to ensure session is restored on page reload or app load
onMounted(() => {
  checkSession(); // This restores the session state on app load or refresh
});
</script>

<style>
/* Apply night mode styles globally */
body.night-mode {
  background-color: #2c3e50;
  color: white;
}

/* Optionally, you can add transitions to make the switch smoother */
body {
  transition: background-color 0.5s, color 0.5s;
}

/* Style for navbar links and toggle button */
body.night-mode button {
  color: white;
}

body button {
  color: black;
}
</style>
