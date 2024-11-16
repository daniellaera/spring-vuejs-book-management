<template>
  <div class="book-list">
    <h2 class="book-list-title">Books</h2>
    <div v-if="books.length === 0" class="loading-message">
      <p>Loading books...</p>
    </div>

    <div v-else>
      <table class="book-table">
        <thead>
        <tr>
          <th>ISBN</th>
          <th>Title</th>
          <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="book in books" :key="book.id">
          <td>{{ book.isbn }}</td>
          <td>{{ book.title }}</td>
          <td>
            <button @click="goToDetail(book.id)" class="action-btn">Details</button>

            <!-- Comment button with badge showing the number of comments -->
            <button
              v-if="isLoggedIn"
              @click="goToDetail(book.id)"
              class="action-btn comment-btn">
              Comment
              <span v-if="book.comments.length" class="comment-badge">{{ book.comments.length }}</span>
            </button>

            <!-- Disabled button when user is not logged in -->
            <button v-else class="action-btn comment-btn" disabled>Login to Comment</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal for "Feature still in progress" -->
    <div v-if="showModal" class="modal">
      <div class="modal-content">
        <p>Feature still in progress</p>
        <button @click="closeModal" class="close-btn">Close</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import { isLoggedIn } from "@/service/useSession";

const books = ref<BookDTO[]>([]);
const router = useRouter();
const showModal = ref(false); // To control the visibility of the modal

const fetchBooks = async () => {
  try {
    const response = await apiClient.get('/book');
    books.value = response.data;
  } catch (error) {
    console.error('Error fetching books:', error);
  }
};

// Adjusted goToDetail to take book.id instead of isbn
const goToDetail = (id: number) => {
  router.push(`/book/${id}`); // Push bookId to the route
};

const closeModal = () => {
  showModal.value = false; // Close the modal
};

onMounted(() => {
  fetchBooks();
});
</script>

<style scoped>
.book-list {
  width: 100%;
  padding: 20px;
}

.book-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}

.book-table th, .book-table td {
  padding: 8px 12px;
  text-align: left;
  border: 1px solid var(--color-border);
}

.book-table th {
  background-color: var(--color-background-soft);
}

.book-table td {
  font-size: 14px;
}

.action-btn {
  padding: 8px 12px;
  margin-right: 10px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
}

.action-btn:hover {
  background-color: #45a049;
}

.comment-btn {
  background-color: #007BFF;
}

.comment-btn:hover {
  background-color: #0056b3;
}

.comment-btn:disabled {
  background-color: #ddd;
  cursor: not-allowed;
}

/* Enhanced Badge Styles */
.comment-badge {
  background-color: #FF5722; /* Vibrant color */
  color: white;
  font-weight: bold;
  border-radius: 12px;  /* Slightly more rounded corners */
  padding: 3px 6px;
  margin-left: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2); /* Adding subtle shadow for contrast */
}

.loading-message {
  font-size: 1.2rem;
  text-align: center;
  color: #888;
}

/* Modal Styles */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 5px;
  text-align: center;
  width: 300px;
}

.close-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 5px;
  cursor: pointer;
}

.close-btn:hover {
  background-color: #45a049;
}

/* Night Mode Styles for Modal Only */
body.night-mode .modal-content {
  background-color: #2c3e50; /* Dark background for modal */
  color: #ffffff; /* Light text for modal */
}

body.night-mode .modal {
  background-color: rgba(0, 0, 0, 0.8); /* Darker background for modal overlay */
}

body.night-mode .close-btn {
  background-color: #4CAF50;
  color: white;
}

body.night-mode .close-btn:hover {
  background-color: #45a049;
}
</style>
