<template>
  <div class="create-book">
    <div class="form-container">
      <h1 class="form-title">Create a New Book</h1>
      <div class="form-content">
        <form @submit.prevent="submitForm">
          <!-- Book Title Input -->
          <div class="form-group">
            <label for="title">Title</label>
            <InputText
              id="title"
              v-model="book.title"
              type="text"
              class="p-inputtext p-component"
              placeholder="Enter the book title"
              required
            />
          </div>

          <!-- Author Input -->
          <div class="form-group">
            <label for="author">Author</label>
            <InputText
              id="author"
              v-model="book.author"
              type="text"
              class="p-inputtext p-component"
              placeholder="Enter the author's name"
              required
            />
          </div>

          <!-- ISBN Input -->
          <div class="form-group">
            <label for="isbn">ISBN</label>
            <InputText
              id="isbn"
              v-model="book.isbn"
              type="text"
              class="p-inputtext p-component"
              placeholder="Enter the ISBN"
              required
            />
          </div>

          <!-- Genre Input -->
          <div class="form-group">
            <label for="genre">Genre</label>
            <InputText
              id="genre"
              v-model="book.genre"
              type="text"
              class="p-inputtext p-component"
              placeholder="Enter the book genre"
              required
            />
          </div>

          <!-- Published Date -->
          <div class="form-group">
            <label for="publishedDate">Published Date</label>
            <DatePicker
              v-model="book.publishedDate"
              :show-icon="true"
              dateFormat="dd/mm/yy"
              placeholder="Select the published date"
              required
            />
          </div>

          <!-- Description Textarea -->
          <div class="form-group">
            <label for="description">Description</label>
            <Textarea
              id="description"
              v-model="book.description"
              class="p-component"
              placeholder="Enter the book description"
              rows="5"
              required
            />
          </div>

          <!-- Submit Button -->
          <Button
            type="submit"
            label="Submit"
            icon="pi pi-check"
            class="p-button-rounded p-mt-3"
            :disabled="!isFormValid"
          />

          <Toast />
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, watch} from "vue";
import InputText from "primevue/inputtext";
import Textarea from "primevue/textarea";
import Button from "primevue/button";
import DatePicker from "primevue/datepicker";
import Toast from "primevue/toast";

import { useToast } from "primevue/usetoast";
import apiClient from "@/plugins/axiosConfig";
import type { BookDTO } from "@/model/book";
import {isLoggedIn} from "@/service/useSession";
import {useRouter} from "vue-router";

const toast = useToast();
const router = useRouter();

// Reactive state for the book form
const book = ref<BookDTO>({
  title: "",
  image: "",
  isbn: "",
  id: 0,
  description: "",
  author: "",
  genre: "",
  comments: [],
  createdDate: new Date(),
  publishedDate: null
});

// Computed property for form validation
const isFormValid = computed(() => {
  return (
    book.value.title.trim() !== "" &&
    book.value.author.trim() !== "" &&
    book.value.isbn.trim() !== "" &&
    book.value.description.trim() !== "" &&
    !!book.value.publishedDate // Ensure it's not undefined or null
  );
});

// Function to handle form submission
const submitForm = async () => {
  try {
    const formattedBook = {
      ...book.value,
      publishedDate: book.value.publishedDate?.toISOString(),
    };

    const token = localStorage.getItem('auth_token');

    const response = await apiClient.post("/book", formattedBook, {
      headers: { Authorization: `Bearer ${token}` }, // Pass the token in the header
    });

    toast.add({
      severity: "success",
      summary: "Success",
      detail: "Book created successfully!",
      life: 3000,
    });

    resetForm();
  } catch (error) {
    console.log('error creating a book', error)
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to create book.",
      life: 3000,
    });
  }
};

// Function to reset the form fields after submission
const resetForm = () => {
  book.value = {
    title: "",
    image: "",
    isbn: "",
    id: 0,
    description: "",
    author: "",
    genre: "",
    comments: [],
    createdDate: new Date(),
    publishedDate: null // Use null
  };
};

watch(isLoggedIn, (newVal) => {
  if (!newVal) {
    router.push('/login');
  }
});
</script>

<style scoped>
.form-container {
  max-width: 600px;
  margin: 4rem auto;
  padding: 2rem;
  border: 1px solid #dcdcdc;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-title {
  text-align: center;
  color: #555;
  font-size: 1.8rem;
  margin-bottom: 1rem;
}

.form-content {
  padding: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  font-size: 1rem;
  margin-bottom: 0.5rem;
  color: #555;
}

.p-inputtext,
.p-component {
  width: 100%;
  border-radius: 4px;
}

.p-mt-3 {
  margin-top: 1rem;
}
</style>
