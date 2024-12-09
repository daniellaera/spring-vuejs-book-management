<script setup lang="ts">
import {ref, computed, watch} from 'vue';
import { InputText, Message } from 'primevue';
import PrimeButton from 'primevue/button';
import { useToast } from 'primevue/usetoast';
import apiClient from '@/plugins/axiosConfig';
import {isLoggedIn, updateUserDetails} from "@/service/useSession";
import type { SignUpRequest } from "@/model/signup-request";
import Toast from "primevue/toast";
import {useRouter} from "vue-router";

// Toast instance
const toast = useToast();
const router = useRouter();

// Single form state object to hold all input fields' data
const userForm = ref<SignUpRequest>({
  email: '',
  password: '',
  firstName: '',
  lastName: '',
});

// State variables
const isSubmitting = ref(false);
const errorMessage = ref<string | null>(null);
const errors = ref({
  firstName: '',
  lastName: '',
});

// Validate the form fields dynamically
const validateForm = () => {
  errors.value.firstName = userForm.value.firstName.trim()
    ? ''
    : 'First name is required.';
  errors.value.lastName = userForm.value.lastName.trim()
    ? ''
    : 'Last name is required.';
};

// Computed property to enable/disable the submit button
const isFormValid = computed(() => {
  return (
    userForm.value.firstName.trim() !== '' &&
    userForm.value.lastName.trim() !== '' &&
    !errors.value.firstName &&
    !errors.value.lastName
  );
});

// Handle form submission
const submitForm = async () => {
  validateForm();

  if (!isFormValid.value) return; // Ensure we only submit if the form is valid

  if (!isLoggedIn.value) {
    errorMessage.value = 'You must be logged in to submit the form.';
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = null;

  try {
    const token = localStorage.getItem('auth_token');
    const response = await apiClient.patch(
      '/auth',
      userForm.value,
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    );

    console.log('Form submission successful:', response.data);

    await updateUserDetails(apiClient);

    // Show success toast
    toast.add({
      severity: "success",
      summary: "Success",
      detail: "User details updated successfully!",
      life: 3000,
    });

    resetForm();
  } catch (error: any) {
    console.error('Form submission failed:', error);
    errorMessage.value =
      error.response?.data?.message || 'An unexpected error occurred.';
    toast.add({
      severity: "error",
      summary: "Error",
      detail: errorMessage.value,
      life: 3000,
    });
  } finally {
    isSubmitting.value = false;
  }
};

// Reset the form fields
const resetForm = () => {
  userForm.value = {
    email: '',
    password: '',
    firstName: '',
    lastName: '',
  };
  errors.value = {
    firstName: '',
    lastName: '',
  };
};

watch(isLoggedIn, (newVal) => {
  if (!newVal) {
    router.push('/login'); // Redirect to login if session expires
  }
});

</script>

<template>
  <div class="edit-profile">
    <form @submit.prevent="submitForm">
      <h2>Edit Profile</h2>

      <!-- First Name Input -->
      <div class="form-group">
        <label for="firstName">First Name</label>
        <InputText
          v-model="userForm.firstName"
          id="firstName"
          placeholder="Enter your first name"
          @input="validateForm"
          :class="{ 'p-invalid': errors.firstName }"
        />
        <Message
          v-if="errors.firstName"
          severity="error"
          size="small"
          variant="simple"
        >
          {{ errors.firstName }}
        </Message>
      </div>

      <!-- Last Name Input -->
      <div class="form-group">
        <label for="lastName">Last Name</label>
        <InputText
          v-model="userForm.lastName"
          id="lastName"
          placeholder="Enter your last name"
          @input="validateForm"
          :class="{ 'p-invalid': errors.lastName }"
        />
        <Message
          v-if="errors.lastName"
          severity="error"
          size="small"
          variant="simple"
        >
          {{ errors.lastName }}
        </Message>
      </div>

      <!-- Submit Button -->
      <PrimeButton
        label="Save"
        icon="pi pi-save"
        class="btn"
        :disabled="isSubmitting || !isFormValid"
        type="submit"
      />

      <Toast />

      <!-- Error Message -->
      <div v-if="errorMessage" class="error-message">
        <Message severity="error">
          <p>{{ errorMessage }}</p>
        </Message>
      </div>
    </form>
  </div>
</template>

<style scoped>
.edit-profile {
  max-width: 400px;
  margin: 0 auto;
  padding: 2rem;
  background-color: var(--color-background-light);
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  margin-top: 80px;
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

.p-invalid {
  border-color: red !important;
}

button.btn {
  width: 100%;
  padding: 0.8rem;
  background-color: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

button.btn:hover {
  background-color: #2ecc71;
}

.error-message {
  color: red;
  text-align: center;
  margin-top: 1rem;
}
</style>
