<template>
  <div class="signup-form">
    <form @submit.prevent="handleSignUp">
      <h2>Create Account</h2>

      <div class="form-group">
        <label for="email">Email</label>
        <input v-model="signUpData.email" type="email" id="email" placeholder="Enter your email" required />
      </div>

      <div class="form-group">
        <label for="password">Password</label>
        <input v-model="signUpData.password" type="password" id="password" placeholder="Enter your password" required />
      </div>

      <button type="submit" class="btn">Sign Up</button>

      <!-- Show error message if login failed -->
      <div v-if="errorMessage" class="error-message">
        <p>{{ errorMessage }}</p>
      </div>


      <div class="login-link">
        <p>Already have an account? <RouterLink to="/login">Login</RouterLink></p>
      </div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import apiClient from '@/plugins/axiosConfig';
import { useRouter } from 'vue-router';

export default defineComponent({
  name: 'SignUpForm',
  setup() {
    const router = useRouter();

    const signUpData = ref({
      email: '',
      password: ''
    });

    const errorMessage = ref<string | null>(null);  // Reactive error message

    const handleSignUp = async () => {
      try {
        const response = await apiClient.post('/auth/signup', signUpData.value);
        console.log('Sign Up Success', response.data);

        // Optionally, redirect after successful sign-up
        await router.push('/login');
      } catch (error: any) {
        if (error.response) {
          // If the response contains a message (like "Invalid username or password")
          errorMessage.value = error.response.data || 'An error occurred. Please try again.';
        } else {
          errorMessage.value = 'An unknown error occurred. Please try again.';
        }
        console.error('Sign Up Failed', error);
      }
    };

    return {
      signUpData,
      errorMessage,
      handleSignUp
    };
  }
});
</script>

<style scoped>
.signup-form {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--color-background-light); /* Default light background */
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  margin-top: 80px; /* Make sure it's not overlapped by the navbar */
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

/* Sign up button styling */
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
body.night-mode .signup-form {
  background-color: #0b1218; /* Darker background for night mode */
}

body.night-mode button.btn {
  color: white; /* White text for buttons in night mode */
}

body.night-mode button.btn:hover {
  background-color: #2ecc71; /* Same green, but text white */
}

/* Link section */
.login-link {
  text-align: center;
  margin-top: 1.5rem;
}

.login-link p {
  font-size: 0.9rem;
}

.login-link a {
  color: #27ae60;
  text-decoration: none;
  font-weight: bold;
}

.login-link a:hover {
  text-decoration: underline;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 1rem;
}
</style>
