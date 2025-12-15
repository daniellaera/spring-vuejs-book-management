import { ref } from 'vue';

// todo not used??
export function useNightMode() {
  const isNightMode = ref(false);

  // Initialize the night mode state based on localStorage or default to false.
  const storedMode = localStorage.getItem('nightMode');
  if (storedMode) {
    isNightMode.value = storedMode === 'true';
  }

  function toggleNightMode() {
    isNightMode.value = !isNightMode.value;
    localStorage.setItem('nightMode', isNightMode.value.toString());
  }

  return {
    isNightMode,
    toggleNightMode // todo not used??
  };
}
