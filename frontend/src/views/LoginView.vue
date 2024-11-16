<template>
  <div class="login-form">
    <form @submit.prevent="handleLogin">
      <h2>Login</h2>

      <div class="form-group">
        <label for="email">Email</label>
        <input v-model="loginData.email" type="email" id="email" placeholder="Enter your email" required />
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input v-model="loginData.password" type="password" id="password" placeholder="Enter your password" required />
      </div>

      <button type="submit" class="btn">Login</button>

      <div class="register-link">
        <p>Don't have an account? <RouterLink to="/signup">Register</RouterLink></p>
      </div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import apiClient from '@/plugins/axiosConfig';
import { useRouter } from 'vue-router';
import { setTokenExpiration } from '@/service/useSession';

export default defineComponent({
  name: 'LoginView',
  setup() {
    const router = useRouter();

    const loginData = ref({
      email: '',
      password: ''
    });

    const handleLogin = async () => {
      try {
        const response = await apiClient.post('/auth/signin', loginData.value);
        const token = response.data.token;
        const user = response.data.username;

        localStorage.setItem('auth_token', token);
        localStorage.setItem('username', user);
        setTokenExpiration(token);

        await router.push('/');

      } catch (error) {
        console.error('Login Failed', error);
        localStorage.removeItem('auth_token');
        localStorage.removeItem('username');
      }
    };

    return {
      loginData,
      handleLogin
    };
  }
});
</script>

<style scoped>
.login-form {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--color-background-light); /* Default light background */
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  margin-top: 80px; /* Ensure it's not overlapped by the navbar */
}

/* Heading */
h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

/* Form groups */
.form-group {
  margin-bottom: 1.2rem;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

input {
  width: 100%;
  padding: 0.8rem;
  margin-bottom: 0.8rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

input:focus {
  border-color: #27ae60;
  outline: none;
}

/* Login button styling */
button.btn {
  width: 100%;
  padding: 0.8rem;
  background-color: #27ae60; /* Green background */
  color: black; /* Default text color (light mode) */
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease; /* Add color transition */
}

button.btn:hover {
  background-color: #2ecc71; /* Lighter green on hover */
  color: black; /* Keep text black on hover in light mode */
}

/* Night mode styles */
body.night-mode .login-form {
  background-color: #0b1218; /* Darker background for night mode */
}

body.night-mode button.btn {
  color: white; /* White text for buttons in night mode */
}

body.night-mode button.btn:hover {
  background-color: #2ecc71; /* Same green, but text white */
}

/* Link section */
.register-link {
  text-align: center;
  margin-top: 1.5rem;
}

.register-link p {
  font-size: 0.9rem;
}

.register-link a {
  color: #27ae60;
  text-decoration: none;
  font-weight: bold;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
