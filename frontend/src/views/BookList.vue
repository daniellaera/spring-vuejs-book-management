<template>
  <div class="card">
    <!-- Loading State -->
    <div v-if="loading" class="loading-message">Loading books...</div>

    <!-- No Books Available State -->
    <div v-else-if="books.length === 0" class="loading-message">
      No books available
    </div>

    <!-- DataTable will show once loading is finished -->
    <DataTable v-else
               :value="filteredBooks"
               tableStyle="min-width: 50rem"
               :sortField="sortField"
               :sortOrder="sortOrder"
               @sort="onSort"
               @page="onPage"
               :loading="loading"
               :rows="rows"
               :paginator="true"
               :totalRecords="totalRecords"
               lazy
               :first="page * rows"
    >
      <!-- Table Header -->
      <template #header>
        <div class="flex items-center justify-between gap-6">
          <h2 class="text-xl font-bold">Books</h2>
          <Button icon="pi pi-refresh" label="Refresh" rounded raised @click="fetchBooks" />
          <div>
            <IconField>
              <InputIcon>
                <i class="pi pi-search"/>
              </InputIcon>
              <InputText v-model="searchTerm" placeholder="Search by title or ISBN"/>
            </IconField>
          </div>
        </div>
      </template>

      <!-- ISBN Column -->
      <Column field="isbn" header="ISBN" :sortable="true"></Column>

      <!-- Title Column -->
      <Column field="title" header="Title" :sortable="true"></Column>

      <!-- Published Date Column -->
      <Column
        field="publishedDate"
        header="Published Date"
        :sortable="true"
      >
        <template #body="slotProps">
          {{ formatDate(slotProps.data.publishedDate) }}
        </template>
      </Column>

      <Column header="Borrow Status">
        <template #body="slotProps">
          <div class="borrow-status">
            <i
              v-if="slotProps.data.borrow && !slotProps.data.borrow.isReturned"
              class="pi pi-check-circle text-green-500"
              v-tooltip="'Borrowed'"
            ></i>
            <i
              v-else
              class="pi pi-times-circle text-red-500"
              v-tooltip="'Available'"
            ></i>
          </div>
        </template>
      </Column>

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

      <!-- Average Rating Column -->
      <Column header="Average Rating">
        <template #body="slotProps">
          <Rating
            v-model="slotProps.data.averageRating"
            readonly
            :stars="5"
            cancel="false"
            v-tooltip="slotProps.data.averageRating.toFixed(1)"
          />
        </template>
      </Column>

      <!-- Actions Column -->
      <Column header="Actions">
        <template #body="slotProps">
          <div class="flex gap-4 items-center">
            <Button
              label="Details"
              icon="pi pi-info-circle"
              outlined
              @click="goToDetail(slotProps.data.id)"
            />
            <div v-if="isLoggedIn">
              <Button
                label="Comment"
                icon="pi pi-comment"
                severity="primary"
                outlined
                @click="goToDetail(slotProps.data.id)"
                :badge="String(slotProps.data.comments?.length || 0)"
              />
            </div>
            <div v-else>
              <Button
                label="Login to Comment"
                icon="pi pi-lock"
                severity="secondary"
                outlined
                disabled
              />
            </div>
          </div>
        </template>
      </Column>

      <!-- Footer -->
      <template #footer>
        <div class="text-right">In total there are {{ totalRecords }} books.</div>
      </template>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import { isLoggedIn } from "@/service/useSession";

// PrimeVue Components
import DataTable, { type DataTableSortEvent } from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Rating from 'primevue/rating';
import InputText from 'primevue/inputtext';
import InputIcon from 'primevue/inputicon';
import IconField from 'primevue/iconfield';

const books = ref<BookDTO[]>([]);
const loading = ref(true);
const page = ref(0); // Current page index
const rows = ref(5); // Number of rows per page
const sortField = ref('publishedDate'); // Current sorting field, default to `publishedDate`
const sortOrder = ref<number>(1); // Sorting order (1 for ascending, -1 for descending)
const searchTerm = ref(''); // Search term input
const totalRecords = ref(0); // Total records count
const router = useRouter();

// Computed for filtered books based on search
const filteredBooks = computed(() => {
  if (searchTerm.value.trim() === '') {
    return books.value; // If no search term, show all books
  }
  return books.value.filter(book =>
    book.title.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
    book.isbn.includes(searchTerm.value)
  );
});

const fetchBooks = async () => {
  loading.value = true;

  try {
    const sortParameter = sortField.value
      ? `${sortField.value},${sortOrder.value === 1 ? 'asc' : 'desc'}`
      : null;

    const response = await apiClient.get('/book', {
      params: {
        page: page.value,
        size: rows.value,
        sort: sortParameter,
      },
    });

    books.value = response.data.content || []; // Default to empty array if content is missing
    totalRecords.value = response.data.totalElements || 0;
  } catch (error) {
    console.error('Error fetching books:', error);
    books.value = []; // Fallback to empty array in case of error
  } finally {
    loading.value = false;
  }
};

const onSort = (event: DataTableSortEvent) => {
  // Ensure sortField is a string, otherwise fallback to 'publishedDate'
  sortField.value = typeof event.sortField === 'string' ? event.sortField : 'publishedDate';

  // Ensure sortOrder is properly set
  sortOrder.value = event.sortOrder ?? 1;

  // Re-fetch books with the updated sorting
  fetchBooks();
};

const onPage = (event: { page: number; rows: number }) => {
  if (event.page === page.value && event.rows === rows.value) {
    return; // Prevent unnecessary updates
  }

  page.value = event.page;
  rows.value = event.rows;

  fetchBooks(); // Re-fetch books based on the new page and rows
};

const goToDetail = (id: number) => {
  router.push(`/book/${id}`);
};

const formatDate = (date: Date | null): string => {
  if (!date) return 'N/A';
  return new Date(date).toLocaleDateString();
};

const getRandomImage = () => {
  return `https://picsum.photos/50/50?random=${Math.random()}`;
};

onMounted(() => {
  fetchBooks();
});
</script>

<style scoped>
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

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.gap-6 {
  gap: 1.5rem;
}

.gap-4 {
  gap: 1rem;
}

.loading-message {
  font-size: 1.5rem;
  color: #999;
  text-align: center;
  padding: 1rem;
}
</style>
