<template>
  <section class="home-view">
    <div class="book-list-section">
      <BookList />
    </div>
  </section>
</template>

<script setup lang="ts">
import BookList from "@/views/BookList.vue";
</script>

<style scoped>
/* Home view container styling */
.home-view {
  display: flex;
  justify-content: center; /* Centers content horizontally */
  align-items: flex-start; /* Aligns content to the top of the container */
  padding-top: 80px; /* Adds space for the fixed navbar */
}

.book-list-section {
  width: 100%;
  max-width: 1200px; /* Sets a maximum width for large screens */
  margin: 0 auto; /* Centers the book list section */
}

@media (max-width: 1024px) {
  .home-view {
    padding-top: 60px; /* Adjusts top padding for smaller screens */
  }
}
</style>
