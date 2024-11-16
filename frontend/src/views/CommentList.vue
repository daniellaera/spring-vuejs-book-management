<script setup lang="ts">
import { defineProps } from 'vue';
import type { CommentDTO } from '@/model/comment';

// Define props to accept a list of comments
defineProps({
  comments: {
    type: Array as () => CommentDTO[],
    required: true,
  },
});
</script>

<template>
  <div class="comments-section">
    <!-- Check if comments array is empty or undefined -->
    <div v-if="!comments || comments.length === 0" class="loading-message">
      <p>No comments for this book</p>
    </div>

    <!-- Display comments count if there are any comments -->
    <h4 v-if="comments && comments.length > 0">Comments ({{ comments.length }})</h4>

    <!-- Render the comments list -->
    <ul v-if="comments && comments.length > 0">
      <li v-for="(comment, index) in comments" :key="index">
        <q class="quote-style">{{ comment.content }}</q>
        <span class="author-name"> - {{ comment.authorFullName }}</span>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.comments-section {
  margin-top: 30px;
  padding: 20px;
  border-top: 1px solid #ddd;
}

.comments-section h3 {
  margin-bottom: 15px;
  font-size: 1.5rem;
  color: #333;
}

.comments-section ul {
  list-style: none;
  padding: 0;
}

.comments-section li {
  font-size: 1rem;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.quote-style {
  font-style: italic;
  margin-right: 10px;
}

.author-name {
  font-weight: bold;
  font-style: normal;
}
</style>
