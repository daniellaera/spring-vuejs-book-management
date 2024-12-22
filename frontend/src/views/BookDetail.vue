<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/plugins/axiosConfig';
import type { BookDTO } from "@/model/book";
import CommentList from '@/views/CommentList.vue'; // Import the CommentList component
import CommentForm from '@/components/CommentForm.vue'; // Import the CommentForm component
import Card from 'primevue/card'; // Import PrimeVue Card
import Button from 'primevue/button';
import SubmitRatingForm from "@/components/SubmitRatingForm.vue";
import { sessionState } from "@/service/useSession"; // Import PrimeVue Button

const book = ref<BookDTO | null>(null);
const route = useRoute();
const router = useRouter();

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

const formatPublishedDate = (date: Date | null) => {
  if (!date) return "Not available";

  const parsedDate = new Date(date);
  if (isNaN(parsedDate.getTime())) return "Invalid date";

  return parsedDate.toLocaleDateString("fr-FR"); // Formats as DD/MM/YYYY
};

</script>

<template>
  <Card
    style="
      width: 55rem;
      margin: 50px auto;
      padding: 1rem;
      border-radius: 12px;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    "
  >
    <template #header>
      <img
        alt="Book Header"
        :src="book?.image || `https://picsum.photos/350/150?random=${Math.random()}`"
        style="width: 100%; height: auto; object-fit: cover; border-radius: 8px;"
      />
    </template>
    <template #title>{{ book?.title }}</template>
    <template #subtitle>By {{ book?.author }}</template>
    <template #content>
      <div v-if="book">
        <p><strong>ISBN:</strong> {{ book.isbn }}</p>
        <p><strong>Genre:</strong> {{ book.genre }}</p>
        <p><strong>Description:</strong> {{ book.description }}</p>
        <p><strong>Published date:</strong> {{ formatPublishedDate(book.publishedDate) }}</p>

        <!-- Render the CommentList component and pass comments -->
        <CommentList :comments="book.comments" />

        <!-- Render the CommentForm to submit a new comment -->
        <CommentForm :bookId="book.id" @comment-added="fetchBookDetails(book.id)" />

        <SubmitRatingForm
          v-if="book.userDTO && book.userDTO.id"
          :session-user-id="sessionState.userDetails.userId ?? 0"
          :book-id="book.id"
          :bookRatings="book.ratings"
          @rating-added="fetchBookDetails(book.id)"
        />
      </div>
    </template>
    <template #footer>
      <Button label="Back to Books" class="w-full" @click="goBack" />
    </template>
  </Card>
</template>
