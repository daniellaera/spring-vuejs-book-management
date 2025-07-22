import { reactive, computed } from 'vue';

interface SessionState {
  tokenExpiration: number | null;
  timeLeft: number;
  userDetails: {
    fullName: string | null,
    githubId: string | null,
    userId: number | null
  }; // Store user details
}

// Initialize sessionState as a reactive object
export const sessionState = reactive<SessionState>({
  tokenExpiration: null,
  timeLeft: 0,
  userDetails: { fullName: null, githubId: null, userId: null },
});

// Computed property to check if the user is logged in
export const isLoggedIn = computed(() => {
  return sessionState.tokenExpiration !== null && sessionState.tokenExpiration > Date.now();
});

// Function to set the token expiration and update the countdown
export function setTokenExpiration(token: string) {
  const expiration = getTokenExpiration(token);
  if (expiration) {
    sessionState.tokenExpiration = expiration;
    updateCountdown();
    localStorage.setItem('auth_token', token); // Store the token in localStorage
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
  sessionState.userDetails.fullName = null;
  localStorage.removeItem('auth_token'); // Optionally clear the token from localStorage
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

  if (token) {
    setTokenExpiration(token); // Restore session state
  }
}

// Function to start the session timer to update the countdown every second
let sessionTimer: ReturnType<typeof setInterval> | null = null;
function startSessionTimer() {
  if (sessionTimer) {
    clearInterval(sessionTimer); // Clear any existing timer
  }

  sessionTimer = setInterval(() => {
    updateCountdown();
    if (sessionState.timeLeft <= 0) {
      clearInterval(sessionTimer!); // Stop the timer once the session expires
      sessionTimer = null;
      resetSession(); // Reset session when the countdown reaches zero
    }
  }, 1000);
}

export async function updateUserDetails(apiClient: any) {
  const token = localStorage.getItem('auth_token');
  if (!token) return;

  try {
    const response = await apiClient.get('/auth/me', {
      headers: { Authorization: `Bearer ${token}` },
    });
    sessionState.userDetails.fullName = response.data.fullName; // Update the user details
    sessionState.userDetails.githubId = response.data.githubId || null; // Default to null if githubId is missing
    sessionState.userDetails.userId = response.data.userId || null; // Default to null if githubId is missing
  } catch (error) {
    console.error('Failed to fetch user details:', error);
    resetSession(); // Reset session on error
  }
}
