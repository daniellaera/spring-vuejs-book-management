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

      <!-- Show error message if login failed -->
      <div v-if="errorMessage" class="error-message">
        <p>{{ errorMessage }}</p>
      </div>

      <div class="oauth-buttons">
        <p>Or log in with:</p>
        <button @click="redirectToGithubOAuth" type="button" class="btn oauth-btn">
          <img src="/assets/github-icon.svg" alt="GitHub Icon" class="github-icon" style="margin-right: 8px;" />
          GitHub
        </button>
      </div>

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

    const errorMessage = ref<string | null>(null);  // Reactive error message

    const handleLogin = async () => {
      try {
        const response = await apiClient.post('/auth/signin', loginData.value);
        const token = response.data.token;
        const user = response.data.username;

        localStorage.setItem('auth_token', token);
        localStorage.setItem('username', user);
        setTokenExpiration(token);

        await router.push('/');

        errorMessage.value = null; // Clear error message if login is successful
      } catch (error: any) {
        console.error('Login Failed', error);

        if (error.response) {
          errorMessage.value = error.response.data || 'An error occurred. Please try again.';
        } else {
          errorMessage.value = 'An unknown error occurred. Please try again.';
        }

        localStorage.removeItem('auth_token'); // Clear the local storage on failed login attempt
        localStorage.removeItem('username');
      }
    };

    const redirectToGithubOAuth = () => {
      // Make a request to your backend to start the OAuth process
      apiClient.get('/auth/github/login')
        .then(response => {
          // This will redirect the user to GitHub's authorization page
          window.location.href = response.data.authUrl; // The backend should send this URL
        })
        .catch(error => {
          console.error('GitHub OAuth redirect failed', error);
        });
    };

    return {
      loginData,
      handleLogin,
      errorMessage,
      redirectToGithubOAuth
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

/* Error message styling */
.error-message {
  color: red;
  text-align: center;
  margin-top: 1rem;
}

.oauth-buttons {
  text-align: center; /* Center content horizontally */
  margin-top: 1.5rem; /* Add spacing above the section */
}

/* Ensure GitHub OAuth button gets the correct styling */
.oauth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 15px;
  font-size: 16px;
  background-color: #0366d6; /* GitHub blue */
  color: #ffffff; /* White text */
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  width: auto;
  margin: 0 auto;
}

.oauth-btn:hover {
  background-color: #0288d1; /* Slightly darker blue on hover */
}

.github-icon {
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px; /* Spacing between icon and text */
}

/* Additional styling to make sure no interference from other button styles */
button[type="submit"].btn {
  background-color: #27ae60 !important; /* Green background for submit button */
}

button[type="button"].btn.oauth-btn {
  background-color: #0366d6 !important; /* GitHub blue background */
}
</style>
