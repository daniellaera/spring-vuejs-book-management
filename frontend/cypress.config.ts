import { defineConfig } from "cypress";

export default defineConfig({
  component: {
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
  },

  e2e: {
    setupNodeEvents() {
      // Event listeners (if any)
    },

    // Correct the specPattern to your actual file structure
    specPattern: 'cypress/e2e/**/*.spec.ts', // Ensure this matches your test file location
    baseUrl: 'http://localhost:5173',
  },
});
