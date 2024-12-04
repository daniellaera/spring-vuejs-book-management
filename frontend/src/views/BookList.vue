<template>
  <div class="card">
    <!-- Loading State -->
    <div v-if="loading" class="loading-message">Loading books...</div>

    <!-- No Books Available State -->
    <div v-else-if="books.length === 0" class="loading-message">
      No books available
    </div>

    <!-- DataTable will show once loading is finished -->
    <DataTable v-else :value="books" tableStyle="min-width: 50rem">
      <!-- Table Header -->
      <template #header>
        <div class="flex items-center justify-between gap-6"> <!-- Increased gap -->
          <h2 class="text-xl font-bold">Books</h2> <!-- No margin to h2, handled by gap -->
          <Button icon="pi pi-refresh" label="Refresh" rounded raised @click="fetchBooks" />
        </div>
      </template>

      <!-- ISBN Column -->
      <Column field="isbn" header="ISBN"></Column>

      <!-- Title Column -->
      <Column field="title" header="Title"></Column>

      <!-- Image Column -->
      <Column header="Image">
        <template #body="slotProps">
          <img
            :src="getRandomImage()"
            :alt="`Random Book Cover`"
            class="w-24 rounded"
          />
        </template>
      </Column>

      <!-- Actions Column -->
      <Column header="Actions">
        <template #body="slotProps">
          <div class="flex gap-4 items-center">
            <!-- Details Button -->
            <Button
              label="Details"
              icon="pi pi-info-circle"
              outlined
              @click="goToDetail(slotProps.data.id)"
              class="p-button-outlined"
            />

            <!-- Comment Button with Badge if Logged In -->
            <div v-if="isLoggedIn">
              <Button
                label="Comment"
                icon="pi pi-comment"
                severity="primary"
                outlined
                @click="goToDetail(slotProps.data.id)"
                :badge="String(slotProps.data.comments?.length || 0)"
                badgeSeverity="info"
                class="p-button-outlined"
              />
            </div>

            <!-- Login to Comment Button if Not Logged In -->
            <div v-else>
              <Button
                label="Login to Comment"
                icon="pi pi-lock"
                severity="secondary"
                outlined
                disabled
                class="p-button-outlined"
              />
            </div>
          </div>
        </template>
      </Column>

      <!-- Footer -->
      <template #footer>
        <div class="text-right">In total there are {{ books ? books.length : 0 }} books.</div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import { isLoggedIn } from "@/service/useSession";

// PrimeVue Components
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';

const books = ref<BookDTO[]>([]);
const loading = ref(true); // Loading state
const router = useRouter();

// Fetch books from backend
const fetchBooks = async () => {
  try {
    const response = await apiClient.get('/book');
    books.value = response.data;
  } catch (error) {
    console.error('Error fetching books:', error);
  } finally {
    loading.value = false; // Set loading to false once data is fetched
  }
};

// Go to book details page
const goToDetail = (id: number) => {
  router.push(`/book/${id}`); // Push bookId to the route
};

// Get a random image from Picsum
const getRandomImage = () => {
  return `https://picsum.photos/50/50?random=${Math.random()}`;
};

onMounted(() => {
  fetchBooks();
});
</script>

<style scoped>
/* Optional Custom Styles for Customization */
.card {
  padding: 1rem;
}

button {
  transition: all 0.3s ease;
}

button:hover {
  background-color: #f3f3f3;
}

.p-button-outlined {
  padding: 0.5rem 1rem;
}

/* Flexbox utility for spacing between buttons */
.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.gap-6 {
  gap: 1.5rem; /* Increased gap between elements */
}

.gap-4 {
  gap: 1rem; /* Adds space between elements */
}

.loading-message {
  font-size: 1.5rem;
  color: #999;
  text-align: center;
  padding: 1rem;
}
</style>
