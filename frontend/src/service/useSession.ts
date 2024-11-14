import { reactive, computed } from 'vue';

interface SessionState {
  tokenExpiration: number | null;
  timeLeft: number;
}

// Initialize sessionState as a reactive object
export const sessionState = reactive<SessionState>({
  tokenExpiration: null,
  timeLeft: 0
});

// Computed property to check if the user is logged in
export const isLoggedIn = computed(() => {
  return sessionState.tokenExpiration !== null && sessionState.tokenExpiration > Date.now();
});

// Function to set the token expiration and update the countdown
export function setTokenExpiration(token: string, username?: string) {
  const expiration = getTokenExpiration(token);
  if (expiration) {
    sessionState.tokenExpiration = expiration;
    updateCountdown(); // Immediately update the countdown after setting expiration
    localStorage.setItem('auth_token', token); // Store the token in localStorage

    if (username) {
      localStorage.setItem('username', username); // Store the username if provided
    }

    startSessionTimer(); // Start a session timer to update countdown
  }
}

// Function to update the countdown based on the token expiration
export function updateCountdown() {
  if (sessionState.tokenExpiration) {
    const now = Date.now();
    sessionState.timeLeft = Math.max(0, Math.floor((sessionState.tokenExpiration - now) / 1000));
  }
}

// Function to reset the session if expired or logged out
export function resetSession() {
  sessionState.tokenExpiration = null;
  sessionState.timeLeft = 0;
  localStorage.removeItem('auth_token'); // Optionally clear the token from localStorage
  localStorage.removeItem('username');
}

// Function to parse the token and extract the expiration timestamp (exp)
function getTokenExpiration(token: string): number | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1])); // Decode and parse the token payload
    return payload.exp * 1000; // Convert `exp` from seconds to milliseconds
  } catch (error) {
    console.error('Failed to parse token', error);
    return null;
  }
}

// Function to check if the user is logged in after page reload
export function checkSession() {
  const token = localStorage.getItem('auth_token');
  const username = localStorage.getItem('username');

  // If the token exists in localStorage, try to restore session
  if (token) {
    if (username) {
      setTokenExpiration(token, username); // Set the expiration and username if available
    } else {
      setTokenExpiration(token); // If no username, still set the expiration
    }
  }
}

// Function to start the session timer to update the countdown every second
let sessionTimer: ReturnType<typeof setInterval> | null = null;
function startSessionTimer() {
  if (sessionTimer) {
    clearInterval(sessionTimer); // Clear any existing timer
  }

  // Update countdown every second
  sessionTimer = setInterval(() => {
    updateCountdown(); // Update countdown every second
    if (sessionState.timeLeft <= 0) {
      clearInterval(sessionTimer!); // Stop the timer once the session has expired
      sessionTimer = null;
      resetSession(); // Reset session when the countdown reaches zero
    }
  }, 1000);
}