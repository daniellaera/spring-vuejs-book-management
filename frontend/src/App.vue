<template>
  <NavBar />
  <!-- RouterView to render the current view like HomeView -->
  <RouterView />
</template>

<script setup lang="ts">
import NavBar from '@/views/NavBar.vue';

// Accessing night mode state and toggle function from the composable

// Watch for changes in the night mode state and toggle the body class
import {onMounted} from 'vue';
import {checkSession} from "@/service/useSession";

// Call checkSession to ensure the session is restored on page reload or app load
onMounted(() => {
  checkSession(); // This restores the session state on app load or refresh
});
</script>

<style>
/* Importing Google Fonts for Poppins */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap');

/* Apply global font family */
* {
  font-family: 'Inter', sans-serif; /* Applies to all elements */
  margin: 0;
  padding: 0;
}

</style>
