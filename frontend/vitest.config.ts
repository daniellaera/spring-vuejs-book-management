import { defineConfig } from 'vitest/config';
import path from 'path';
import vue from '@vitejs/plugin-vue'; // Import the Vue plugin

export default defineConfig({
  plugins: [vue()], // Use the Vue plugin
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: 'src/test/setup.ts',  // Point to the setup file
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),  // Resolve the alias '@' to 'src'
    },
  },
});
