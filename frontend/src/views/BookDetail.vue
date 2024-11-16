<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import CommentList from '@/views/CommentList.vue'; // Import the CommentList component
import CommentForm from '@/components/CommentForm.vue'; // Import the CommentForm component

const book = ref<BookDTO | null>(null);
const route = useRoute();
const router = useRouter();  // To navigate back to the book list

// Fetch book details based on the id from the route
const fetchBookDetails = async (bookId: number) => {
  if (isNaN(bookId)) {
    console.error('Invalid book ID');
    return;
  }

  try {
    const response = await apiClient.get(`/book/${bookId}`);
    book.value = response.data;
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
</script>

<template>
  <div v-if="book" class="book-detail">
    <h1>{{ book.title }}</h1>
    <p><strong>ISBN:</strong> {{ book.isbn }}</p>
    <p><strong>Author:</strong> {{ book.userDTO.fullName }}</p>
    <p><strong>Description:</strong> {{ book.description }}</p>

    <!-- Render the CommentList component and pass comments -->
    <CommentList :comments="book.comments" />

    <!-- Render the CommentForm to submit a new comment -->
    <CommentForm :bookId="book.id" @comment-added="fetchBookDetails(book.id)" />

    <!-- Back button placed under the book details -->
    <button @click="goBack" class="back-btn">Back to books</button>
  </div>
</template>

<style scoped>
.book-detail {
  padding: 100px;
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
}

.book-detail h1 {
  margin-bottom: 10px;
  font-size: 2rem;
}

.book-detail p {
  font-size: 1.2rem;
  margin-bottom: 8px;
}

.back-btn {
  background-color: #4CAF50;
  color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 20px;
  display: block;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.back-btn:hover {
  background-color: #45a049;
  box-shadow: 0 6px 8px rgba(0, 0, 0, 0.2);
}
</style>
