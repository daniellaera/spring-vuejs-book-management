<template>
  <div class="login-form">
    <form @submit.prevent="handleLogin" class="flex flex-col gap-4">
      <h2>Login</h2>

      <!-- Email Input -->
      <div class="form-group">
        <label for="email">Email</label>
        <InputText
          v-model="loginData.email"
          type="email"
          id="email"
          placeholder="Enter your email"
          @input="validateForm"
        required
        :class="{'p-invalid': errors.email}"
        />
        <Message
          v-if="errors.email"
          severity="error"
          size="small"
          variant="simple"
        >
          {{ errors.email }}
        </Message>
      </div>

      <!-- Password Input -->
      <div class="form-group">
        <label for="password">Password</label>
        <InputText
          v-model="loginData.password"
          type="password"
          id="password"
          placeholder="Enter your password"
          @input="validateForm"
        required
        :class="{'p-invalid': errors.password}"
        />
        <Message
          v-if="errors.password"
          severity="error"
          size="small"
          variant="simple"
        >
          {{ errors.password }}
        </Message>
      </div>

      <PrimeButton
        type="submit"
        label="Login"
        class="btn"
        :disabled="!loginData.email || !loginData.password || !!errors.email || !!errors.password"
      />

      <!-- Error Message (if login fails) -->
      <div v-if="errorMessage" class="error-message">
        <Message severity="error" text="An error occurred. Please try again.">
          <p>{{ errorMessage }}</p>
        </Message>
      </div>

      <!-- OAuth Login Options -->
      <div class="oauth-buttons">
        <p>Or log in with:</p>
        <PrimeButton
          @click="redirectToGithubOAuth"
          type="button"
          label="GitHub"
          class="oauth-btn"
        >
          <img src="/assets/github-icon.svg" alt="GitHub Icon" class="github-icon" />
          Login with GitHub
        </PrimeButton>
      </div>

      <!-- Register Link -->
      <div class="register-link">
        <p>Don't have an account? <RouterLink to="/signup">Register</RouterLink></p>
      </div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import InputText from 'primevue/inputtext';
import PrimeButton from 'primevue/button';
import Message from 'primevue/message';
import { useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import { setTokenExpiration } from '@/service/useSession';

export default defineComponent({
  name: 'LoginView',
  components: {
    InputText,
    PrimeButton,
    Message,
  },
  setup() {
    const router = useRouter();

    const loginData = ref({
      email: '',
      password: ''
    });

    const errorMessage = ref<string | null>(null);
    const errors = ref({
      email: '',
      password: ''
    });

    // Form validation logic
    const validateForm = () => {
      errors.value.email = !loginData.value.email
        ? 'Email is required.'
        : !/\S+@\S+\.\S+/.test(loginData.value.email)
          ? 'Invalid email format.'
          : '';

      errors.value.password = !loginData.value.password ? 'Password is required.' : '';
    };

    // Form submission handler
    const handleLogin = async () => {
      validateForm();

      if (!errors.value.email && !errors.value.password) {
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
          // If backend returns an error message, display it
          errorMessage.value = error.response?.data?.message || 'Invalid username or password';
          localStorage.removeItem('auth_token'); // Clear local storage on failed login attempt
          localStorage.removeItem('username');
        }
      }
    };

    // Redirect to GitHub OAuth
    const redirectToGithubOAuth = () => {
      apiClient.get('/auth/github/login')
        .then(response => {
          window.location.href = response.data.authUrl;
        })
        .catch(error => {
          console.error('GitHub OAuth redirect failed', error);
        });
    };

    return {
      loginData,
      handleLogin,
      errorMessage,
      errors,
      validateForm, // Return validateForm so it can be used in the template
      redirectToGithubOAuth,
    };
  }
});
</script>

<style scoped>
.login-form {
  max-width: 400px;  /* Adjust form width */
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--color-background-light);
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  margin-top: 80px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem; /* Add gap for spacing between inputs and button */
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.2rem;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

input, .p-inputtext {
  width: 100%;
  padding: 0.8rem;
  margin-bottom: 0.8rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

input:focus, .p-inputtext:focus {
  border-color: #27ae60;
  outline: none;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 1rem;
}

.oauth-buttons {
  text-align: center;
  margin-top: 1.5rem;
}

.oauth-buttons p {
  margin-bottom: 1rem;  /* Add margin to separate text from buttons */
}

.oauth-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.8rem 1rem;  /* Same padding as the login button */
  font-size: 1rem;        /* Same font size */
  background-color: #0366d6; /* Blue color for the button */
  color: #ffffff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;            /* Make the button fill the width */
  margin: 0 auto;
  transition: background-color 0.2s ease; /* Smooth transition for background color */
  box-sizing: border-box; /* Ensure padding is included in button's total width */
}

/* Hover effect for OAuth button */
.oauth-btn:hover {
  background-color: #0277b3; /* Darker blue on hover */
  padding: 0.8rem 1rem;      /* Keep padding consistent */
  transform: none;           /* Prevent size change */
  box-shadow: none;          /* No box-shadow */
}

/* Active state for OAuth button */
.oauth-btn:active {
  background-color: #0277b3; /* Keep the same dark blue when button is pressed */
  transform: none;           /* Prevent any scaling or size change */
}

/* Focus state for OAuth button */
.oauth-btn:focus {
  outline: none;
  background-color: #0277b3; /* Ensure focus color remains consistent */
}

/* Icon inside the button */
.github-icon {
  display: inline-block;
  vertical-align: middle;
  margin-right: 8px;
}

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

/* PrimeVue Button Custom Style */
.btn {
  font-size: 1rem;    /* Set font size to match input fields */
  width: 100%;         /* Make the button fill the available space */
  padding: 0.8rem;     /* Adjust padding to match input fields */
}
</style>
