<script setup lang="ts">
import {ref, onMounted, computed} from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import CommentList from '@/views/CommentList.vue';
import CommentForm from '@/components/CommentForm.vue';
import Card from 'primevue/card';
import Button from 'primevue/button';
import SubmitRatingForm from "@/components/SubmitRatingForm.vue";
import {isLoggedIn, sessionState} from "@/service/useSession";
import DatePicker from 'primevue/datepicker';
import BorrowBookComponent from "@/components/BorrowBookComponent.vue";
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';

import { useConfirm } from "primevue/useconfirm";
import { useToast } from "primevue/usetoast";

const confirm = useConfirm();
const toast = useToast();

const book = ref<BookDTO | null>(null);
const route = useRoute();
const router = useRouter();
const dates = ref<Date[] | [null, null] | null>(null); // Initialize dates
const minDate = ref<Date | undefined>(undefined);  // Define minDate as ref
const maxDate = ref<Date | undefined>(undefined);  // Define maxDate as ref

const confirmDelete = (bookId?: number) => {
  confirm.require({
    message: 'Do you want to delete this book?',
    header: 'Delete book',
    icon: 'pi pi-info-circle',
    rejectLabel: 'Cancel',
    rejectProps: {
      label: 'Cancel',
      severity: 'secondary',
      outlined: true
    },
    acceptProps: {
      label: 'Delete',
      severity: 'danger'
    },
    accept: () => {
      toast.add({ severity: 'info', summary: 'Confirmed', detail: 'Record deleted', life: 3000 });
      deleteBook(bookId);
    },
    reject: () => {
      toast.add({ severity: 'error', summary: 'Rejected', detail: 'You have rejected', life: 3000 });
    }
  });
};

// Check if the logged-in user is the owner of the book
const isOwner = computed(() => {
  return book.value?.userDTO?.id === sessionState.userDetails.userId;
});

// Handle delete book
const deleteBook = async (bookId?: number) => {
  if (!bookId) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Invalid book ID. Unable to delete.',
      life: 3000,
    });
    return;
  }
  try {
    // get the token
    const token = localStorage.getItem('auth_token');

    await apiClient.delete(`/book/${bookId}`, {
      headers: { Authorization : `Bearer ${token}` }
    });
    toast.add({
      severity: 'success',
      summary: 'Deleted',
      detail: 'The book has been deleted successfully!',
      life: 3000,
    });
    await router.push('/'); // Redirect to the book list after deletion
  } catch (error) {
    console.error('Error deleting book:', error);
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: 'Failed to delete the book. Please try again.',
      life: 3000,
    });
  }
};

// Fetch book details based on the id from the route
const fetchBookDetails = async (bookId: number) => {
  if (isNaN(bookId)) {
    console.error('Invalid book ID');
    return;
  }

  try {
    const response = await apiClient.get(`/book/${bookId}`);
    book.value = response.data;

    // Set dates based on current borrow dates
    if (book.value?.borrow && !book.value.borrow.isReturned) {
      dates.value = [new Date(book.value.borrow.borrowStartDate), new Date(book.value.borrow.borrowEndDate)];
    }

  } catch (error) {
    console.error('Error fetching book details:', error);
  }
};

// Fetch book details on component mount based on the id from the route
onMounted(() => {
  const bookId = route.params.id as string;

  // Check if the bookId is valid and fetch details
  if (bookId && !isNaN(Number(bookId))) {
    fetchBookDetails(Number(bookId));  // Convert to number and fetch the book details
  } else {
    console.error('Invalid book ID');
  }
});

// Function to navigate back to the book list
const goBack = () => {
  router.push('/');  // Adjust to the correct path
};

const formatDate = (date: Date | null) => {
  if (!date) return "Not available";

  const parsedDate = new Date(date);
  if (isNaN(parsedDate.getTime())) return "Invalid date";

  return parsedDate.toLocaleDateString("fr-FR"); // Formats as DD/MM/YYYY
};

</script>

<template>
  <Card class="card-container">
    <template #header>
      <img
        alt="Book Header"
        :src="book?.image || `https://picsum.photos/350/150?random=${Math.random()}`"
        class="header-image"
      />
    </template>
    <!-- Title Section -->
    <template #title>
      <h2 class="book-title">{{ book?.title }}</h2>
    </template>

    <!-- Subtitle Section -->
    <template #subtitle>
      <h3 class="book-subtitle">By {{ book?.author }}</h3>
    </template>
    <template #content>
      <div v-if="book">
        <!-- Book Details -->
        <div class="book-details">
          <p><strong>ISBN:</strong> {{ book.isbn }}</p>
          <p><strong>Genre:</strong> {{ book.genre }}</p>
          <p><strong>Description:</strong> {{ book.description }}</p>
          <p><strong>Published date:</strong> {{ formatDate(book.publishedDate) }}</p>
        </div>

        <!-- Borrow Information Section -->
        <!-- Date Picker Section -->
        <div class="card flex justify-center date-picker-section">
          <p v-if="book.borrow && !book.borrow.isReturned">
            <strong>Currently Borrowed:</strong>
            <br />
            <DatePicker
              v-model="dates"
              selectionMode="range"
              format="dd/mm/yy"
              inline
              showWeek
              class="date-picker"
              placeholder="Select Date Range"
              :manualInput="false"
              :minDate="minDate"
              :maxDate="maxDate"
            />
          </p>
          <div v-else>
            <p>No one is currently borrowing this book.</p>
            <BorrowBookComponent
              :book-id="book.id"
              :minDate="minDate"
              :maxDate="maxDate"
              @borrow-range-updated="fetchBookDetails(book.id)"
            />
          </div>
        </div>

        <!-- Comments Section -->
        <div class="comments-section">
          <CommentList :comments="book.comments" />
        </div>

        <!-- Add Comment Section -->
        <div class="add-comment-section">
          <CommentForm :bookId="book.id" @comment-added="fetchBookDetails(book.id)" />
        </div>

        <!-- Submit Rating Section -->
        <div v-if="book.userDTO && book.userDTO.id" class="submit-rating-section">
          <SubmitRatingForm
            :session-user-id="sessionState.userDetails.userId ?? 0"
            :book-id="book.id"
            :bookRatings="book.ratings"
            @rating-added="fetchBookDetails(book.id)"
          />
        </div>
      </div>
    </template>
    <template #footer>
      <Button
        label="Back to Books"
        class="footer-button"
        icon="pi pi-arrow-left"
        iconPos="left"
        @click="goBack"
      />
      <!-- Conditionally render delete button for owner -->
      <div class="owner-actions">
        <Button
          label="Delete Book"
          class="p-button-danger p-button-rounded"
          icon="pi pi-trash"
          iconPos="left"
          :disabled="!isLoggedIn || !isOwner"
          @click="confirmDelete(book?.id)"
        />
      </div>
      <!-- ConfirmDialog for Delete -->
      <ConfirmDialog />
      <Toast />
    </template>
  </Card>
</template>

<style scoped>
.card-container {
  width: 55rem;
  margin: 50px auto;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.header-image {
  width: 100%;
  height: auto;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.book-title {
  margin-bottom: 0.5rem;
}

.book-subtitle {
  margin-bottom: 1rem;
  color: gray;
}

.book-details {
  margin-bottom: 2rem;
}

.date-picker-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
}

.date-picker {
  width: 100%;
  max-width: 30rem;
}

.comments-section {
  margin-bottom: 2rem;
}

.add-comment-section {
  margin-bottom: 2rem;
}

.submit-rating-section {
  margin-bottom: 2rem;
}

.footer-button {
  margin-top: 1rem;
}

.owner-actions {
  margin-top: 1rem;
  text-align: center;
}

.p-button-danger {
  background: linear-gradient(to right, #ff416c, #ff4b2b);
  border: none;
  color: white;
  transition: transform 0.2s;
}

.p-button-danger:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 10px rgba(255, 75, 43, 0.5);
}

.p-button-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

</style>
