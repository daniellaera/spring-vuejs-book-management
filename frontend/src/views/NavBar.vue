<template>
  <div class="card">
    <!-- Menubar with dynamic login/logout options and night mode toggle -->
    <Menubar :model="items">
      <!-- Night mode toggle button inside Menubar -->
      <template #end>
        <div class="menu-right-content">
          <!-- Night mode toggle button -->
          <Button
            :icon="isNightMode ? 'pi pi-moon' : 'pi pi-sun'"
            class="p-button-rounded p-button-text night-mode-toggle"
            @click="toggleNightMode"
          />

          <!-- Session timer displayed as a Badge inside the Menubar -->
          <template v-if="isLoggedIn && sessionState.timeLeft > 0">
            <span>Time left: </span>
            <Badge :value="sessionState.timeLeft" severity="info" />
          </template>

          <!-- Display session expired message in Menubar -->
          <template v-else-if="!isLoggedIn || sessionState.timeLeft <= 0">
            <Badge value="Session Expired" severity="danger" />
          </template>
        </div>
      </template>
    </Menubar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { sessionState, isLoggedIn, updateUserDetails } from '@/service/useSession';
import apiClient from '@/plugins/axiosConfig';
import Menubar from 'primevue/menubar';
import Button from 'primevue/button';
import Badge from 'primevue/badge';
import Tag from 'primevue/tag';

// Router instance
const router = useRouter();

// Track whether night mode is active
const isNightMode = ref(false);

// Function to toggle night mode
const toggleNightMode = () => {
  isNightMode.value = !isNightMode.value;
  document.documentElement.classList.toggle('my-app-dark', isNightMode.value);
  localStorage.setItem('night-mode', isNightMode.value ? 'true' : 'false');
};

// On component mount, check night mode state and fetch user details if logged in
onMounted(() => {
  const savedNightMode = localStorage.getItem('night-mode');
  if (savedNightMode === 'true') {
    isNightMode.value = true;
    document.documentElement.classList.add('my-app-dark');
  } else {
    isNightMode.value = false;
    document.documentElement.classList.remove('my-app-dark');
  }

  if (isLoggedIn.value) {
    updateUserDetails(apiClient);
  }
});

const logout = async () => {
  await logoutUser(); // Calls the logout API
  window.location.reload(); // Reload the page to clear user state
};

const logoutUser = async () => {
  try {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('username');
  } catch (error) {
    console.error('Error logging out:', error);
  }
};

// Watch for login state changes to refresh user details dynamically
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    updateUserDetails(apiClient); // Fetch user details on login
  }
});

// Function to get full name of the user
const getFullName = () => {
  const userDetails = sessionState.userDetails.fullName;
  return userDetails === 'null null' ? 'Guest' : userDetails;
};

// Reactive Menubar items with conditional visibility based on session state
const items = computed(() => [
  {
    label: 'Home',
    icon: 'pi pi-home',
    command: () => router.push('/'), // Navigate to home page
  },
  {
    label: 'User',
    icon: 'pi pi-user-edit',
    items: [
      {
        label: 'Register',
        icon: 'pi pi-user-plus',
        command: () => router.push('/signup'),
        visible: !isLoggedIn.value,
      },
      {
        label: 'Login',
        icon: 'pi pi-sign-in',
        command: () => router.push('/login'),
        visible: !isLoggedIn.value,
      },
      {
        label: 'User Settings',
        icon: 'pi pi-cog',
        command: () => router.push('/edit-user'),
        visible: isLoggedIn.value, // Only visible if logged in
      },
      {
        label: 'Logout',
        icon: 'pi pi-sign-out',
        command: logout,
        visible: isLoggedIn.value,
      },
    ],
  },
  {
    label: 'Create a Book',
    icon: 'pi pi-book',
    command: () => router.push('/create-book'),
    visible: isLoggedIn.value, // Only visible if logged in
  },
  {
    label: `Welcome, ${getFullName()}`,
    icon: () => (sessionState.userDetails.githubId ? 'pi pi-github' : 'pi pi-user'),
    visible: isLoggedIn.value && sessionState.timeLeft > 0, // Show if logged in and session is valid
  },
  {
    label: 'Please Log In',
    icon: 'pi pi-exclamation-triangle',
    disabled: true,
    visible: !isLoggedIn.value || sessionState.timeLeft <= 0, // Show if not logged in or session expired
  },
]);
</script>

<style scoped>
/* Styling for Menubar right content */
.menu-right-content {
  display: flex;
  align-items: center;
  gap: 10px; /* Space between night mode button and badge */
}

/* Styling for night mode toggle button */
.night-mode-toggle {
  padding: 10px 20px;
  background-color: transparent;
  border: none;
  cursor: pointer;
  font-size: 2rem;
  color: var(--color-heading);
  transition: color 0.3s;
}

</style>
