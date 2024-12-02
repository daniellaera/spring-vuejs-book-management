<template>
  <div class="callback">
    <div class="spinner"></div>
    <h2>Processing your login...</h2>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import {setTokenExpiration} from "@/service/useSession";

const router = useRouter();

onMounted(async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get('code');

  if (!code) {
    console.error('No authorization code found in URL.');
    await router.push('/login'); // Redirect to login if no code is found
    // https://stackoverflow.com/questions/64960385/how-can-i-setup-login-with-spring-security-and-vue-js
    return;
  }

  try {
    // Send the code to the backend
    const response = await apiClient.post('/auth/github/callback', { code });

    console.log('Received response:', response);  // Log full response for inspection
    const token = response.data.token;
    setTokenExpiration(token);

    if (!token) {
      console.error('Token is missing in the response.');
      await router.push('/login');
      return;
    }

    console.log('Received token:', token); // Log the received token

    // Store the token locally (in localStorage or a cookie)
    localStorage.setItem('auth_token', token);

    // Redirect the user to the homepage or dashboard
    await router.push('/');
  } catch (error) {
    console.error('GitHub OAuth processing failed:', error);
    alert('Login failed. Please try again.');
    await router.push('/login');
  }
});
</script>

<style scoped>
.callback {
  margin-top: 150px; /* Push the entire content down */
  text-align: center;
  font-family: 'Inter', sans-serif;
}

.spinner {
  margin: 0 auto;
  width: 50px;
  height: 50px;
  border: 6px solid #e5e5e5; /* Light gray background of spinner */
  border-top: 6px solid #27ae60; /* Primary color of spinner */
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

/* Keyframes for the spinner animation */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* Adjust text spacing with spinner */
.callback h2 {
  margin-top: 20px; /* Space between spinner and text */
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-color, #333);
}
</style>
