import {mount, RouterLinkStub} from '@vue/test-utils';
import {describe, it, expect, vi} from 'vitest';
import BookList from '@/views/BookList.vue';
import apiClient from "@/plugins/axiosConfig";

// Mock the API endpoint '/post'
vi.mock('@/plugins/axiosConfig');

// Mock the Vue Router and ensure `useRouter` is returned
vi.mock('vue-router', () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),  // Mock the `push` method for routing
  })),
  RouterLink: RouterLinkStub,  // Stub RouterLink
}));

describe('BookList.vue', () => {
  it('should mount the component without errors', () => {
    const wrapper = mount(BookList);

    // Ensure the component is rendered properly
    expect(wrapper.exists()).toBe(true);
  });
  it('displays an alert when no books are available', async () => {
    // Mock the API response to return an empty array
    vi.mocked(apiClient.get).mockResolvedValueOnce({data: []});

    // Mount the component
    const wrapper = mount(BookList, {
      global: {
        stubs: {
          RouterLink: RouterLinkStub, // Stub RouterLink to avoid warnings
        },
      },
    });

    // Wait for the component to fetch data and update DOM
    await wrapper.vm.$nextTick();

    // Assert that the "Loading books..." message is displayed
    expect(wrapper.text()).toContain('Loading books...');
  });
});
