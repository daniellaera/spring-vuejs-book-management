<template>
  <div class="signup-form">
    <form @submit.prevent="handleSignUp">
      <h2>Create Account</h2>

      <!-- Email Input (PrimeVue InputText) -->
      <div class="form-group">
        <label for="email">Email</label>
        <InputText
          v-model="signUpData.email"
          id="email"
          type="email"
          placeholder="Enter your email"
          required
          @input="validateForm"
          :class="{'p-invalid': errors.email}"
        />
        <Message v-if="errors.email" severity="error" size="small" variant="simple">
          {{ errors.email }}
        </Message>
      </div>

      <!-- Password Input (PrimeVue Password) -->
      <div class="form-group">
        <label for="password">Password</label>
        <Password
          v-model="signUpData.password"
          id="password"
          placeholder="Enter your password"
          required
          @input="validateForm"
          :class="{'p-invalid': errors.password}"
        />
        <Message v-if="errors.password" severity="error" size="small" variant="simple">
          {{ errors.password }}
        </Message>
      </div>

      <!-- Sign Up Button (PrimeVue PrimeButton) -->
      <PrimeButton
        label="Sign Up"
        icon="pi pi-user-plus"
        class="btn"
        :disabled="!signUpData.email || !signUpData.password || !!errors.email || !!errors.password"
        type="submit"
      />

      <!-- Error Message (if signup fails) -->
      <div v-if="errorMessage" class="error-message">
        <Message severity="error" text="An error occurred. Please try again.">
          <p>{{ errorMessage }}</p>
        </Message>
      </div>

      <!-- Login Link -->
      <div class="login-link">
        <p>Already have an account? <RouterLink to="/login">Login</RouterLink></p>
      </div>
    </form>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { InputText, Password, Message } from 'primevue';
import PrimeButton from 'primevue/button';
import apiClient from '@/plugins/axiosConfig';

export default defineComponent({
  name: 'SignUpForm',
  components: {
    InputText,
    Password,
    PrimeButton,
    Message,
  },
  setup() {
    const router = useRouter();

    const signUpData = ref({
      email: '',
      password: '',
    });

    const errorMessage = ref<string | null>(null);  // Reactive error message
    const errors = ref({
      email: '',
      password: '',
    });

    // Form validation logic
    const validateForm = () => {
      errors.value.email = !signUpData.value.email
        ? 'Email is required.'
        : !/\S+@\S+\.\S+/.test(signUpData.value.email)
          ? 'Invalid email format.'
          : '';

      errors.value.password = !signUpData.value.password ? 'Password is required.' : '';
    };

    // Form submission handler
    const handleSignUp = async () => {
      validateForm();

      if (!errors.value.email && !errors.value.password) {
        try {
          const response = await apiClient.post('/auth/signup', signUpData.value);
          console.log('Sign Up Success', response.data);

          // Redirect to login after successful sign-up
          await router.push('/login');
        } catch (error: any) {
          if (error.response) {
            errorMessage.value = error.response.data || 'An error occurred. Please try again.';
          } else {
            errorMessage.value = 'An unknown error occurred. Please try again.';
          }
          console.error('Sign Up Failed', error);
        }
      }
    };

    return {
      signUpData,
      errorMessage,
      errors,
      handleSignUp,
      validateForm,
    };
  },
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

input,
.p-password {
  width: 100%;
  padding: 0.8rem;
  margin-bottom: 0.8rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 1rem;
}

input:focus,
.p-password:focus {
  border-color: #27ae60; /* Green border when focused */
  outline: none;
}

.p-invalid {
  border-color: red !important; /* Red border on invalid fields */
}

/* Sign up button styling */
button.btn,
.p-button {
  width: 100%;
  padding: 0.8rem;
  background-color: #27ae60; /* Green background */
  color: white; /* Text color */
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button.btn:hover,
.p-button:hover {
  background-color: #2ecc71; /* Lighter green on hover */
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 1rem;
}

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
</style>
