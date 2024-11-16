<template>
  <nav class="navbar">
    <RouterLink to="/" class="nav-button">Home</RouterLink>
    <RouterLink v-if="!isLoggedIn" to="/signup" class="nav-button">Register</RouterLink>
    <RouterLink v-if="!isLoggedIn" to="/login" class="nav-button">Login</RouterLink>
    <button v-if="isLoggedIn" @click="logout" class="nav-button">Logout</button>

    <!-- Welcome message with Full Name -->
    <div v-if="isLoggedIn && sessionState.timeLeft > 0" class="welcome-message">
      Welcome back, {{ sessionState.userDetails.fullName || 'Guest' }}
    </div>

    <!-- Countdown timer or session expired message -->
    <div v-if="isLoggedIn && sessionState.timeLeft > 0" class="countdown-timer">
      Time left in session: {{ sessionState.timeLeft }} seconds
    </div>
    <div v-else-if="!isLoggedIn || sessionState.timeLeft <= 0" class="session-expired">
      Your session has expired. Please log in again.
    </div>

    <!-- Night mode toggle button -->
    <button @click="toggleNightMode" class="night-mode-toggle">
      <span v-if="isNightMode">🌙</span> <!-- Moon icon for night mode -->
      <span v-else>🌞</span> <!-- Sun icon for day mode -->
    </button>
  </nav>
</template>

<script setup lang="ts">
import { sessionState, isLoggedIn, updateUserDetails } from '@/service/useSession';
import { ref, onMounted, watch } from 'vue';
import apiClient from "@/plugins/axiosConfig";

// Track whether night mode is active
const isNightMode = ref(false);

// Function to toggle night mode
const toggleNightMode = () => {
  isNightMode.value = !isNightMode.value;
  document.body.classList.toggle('night-mode', isNightMode.value);
  localStorage.setItem('night-mode', isNightMode.value ? 'true' : 'false');
};

// On component mount, check night mode state and fetch user details if logged in
onMounted(() => {
  // Restore night mode state
  const savedNightMode = localStorage.getItem('night-mode');
  if (savedNightMode === 'true') {
    isNightMode.value = true;
    document.body.classList.add('night-mode');
  } else {
    isNightMode.value = false;
    document.body.classList.remove('night-mode');
  }

  // Fetch user details only if logged in
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
</script>

<style scoped>
/* Navbar styling */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--color-background-soft);
  padding: 1rem 2rem;
  border-bottom: 2px solid var(--color-border);
  position: fixed;  /* Sticks the navbar at the top */
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000; /* Makes sure the navbar stays on top of other content */
  box-sizing: border-box;
}

/* Navbar link styling */
.nav-button {
  font-size: 1.1em;
  padding: 12px 28px;
  text-decoration: none;
  border: 2px solid transparent;
  border-radius: 5px;
  color: var(--color-heading);
  background-color: var(--color-background-mute);
  transition: background-color 0.3s, color 0.3s, border-color 0.3s;
  margin: 0 10px;
  display: inline-block;
  box-sizing: border-box;
}

/* Hover effect for nav buttons */
.nav-button:hover {
  background-color: var(--color-heading);
  border-color: var(--color-heading);
}

/* Countdown timer and session expired message */
.countdown-timer {
  font-size: 1em;
  color: var(--color-heading);
  font-weight: bold;
}

.session-expired {
  font-size: 1em;
  color: #e74c3c; /* Red color for expired session */
  font-weight: bold;
}

/* Night mode toggle button styling */
.night-mode-toggle {
  padding: 10px 20px;
  background-color: transparent; /* No background for icon button */
  border: none;
  cursor: pointer;
  font-size: 2rem; /* Larger icon */
  color: var(--color-heading); /* Default color in light mode */
  transition: color 0.3s;
  display: inline-flex;
  align-items: center;
}

/* Hover effect for the moon/sun button */
.night-mode-toggle:hover {
  color: var(--color-heading); /* Ensure the color matches when hovering */
}

/* Night mode specific styles */
body.night-mode .navbar {
  background-color: #0b1218; /* Dark background for navbar in night mode */
}

body.night-mode .night-mode-toggle {
  color: #fff; /* White moon/sun icon in night mode */
}
</style>
