import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock vue-router globally
vi.mock('vue-router', () => ({
  RouterLink: { render: () => null },  // Mocking RouterLink to avoid Vue warnings
}));
