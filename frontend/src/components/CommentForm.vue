<script setup lang="ts">
import { ref } from 'vue';
import { isLoggedIn } from "@/service/useSession"; // Import the session management
import apiClient from '@/plugins/axiosConfig'; // Axios configuration

// Props for the bookId
const props = defineProps({
  bookId: {
    type: Number,
    required: true,
  },
});

// Emit event to notify parent about a new comment
const emit = defineEmits(['comment-added']);

// Reactive state for form data
const commentContent = ref('');
const isSubmitting = ref(false);
const errorMessage = ref<string | null>(null);

// Function to handle comment submission
const submitComment = async () => {
  if (!commentContent.value.trim()) {
    errorMessage.value = 'Comment cannot be empty.';
    return;
  }

  if (commentContent.value.length > 500) {
    errorMessage.value = 'Comment cannot exceed 500 characters.';
    return;
  }

  // Ensure user is logged in before allowing them to post
  if (!isLoggedIn.value) {
    errorMessage.value = 'You must be logged in to post a comment.';
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = null;

  try {
    // Making the API request with the user’s token included in the Authorization header
    const token = localStorage.getItem('auth_token');
    const response = await apiClient.post(`/comment/${props.bookId}`, {
      content: commentContent.value,
    }, {
      headers: { Authorization: `Bearer ${token}` }, // Pass the token in the header
    });

    // Emit the new comment to the parent component
    emit('comment-added', response.data);

    // Clear the input field after submission
    commentContent.value = '';
  } catch (error: any) {
    console.error('Error submitting comment:', error);
    if (error.response && error.response.status === 401) {
      errorMessage.value = 'Unauthorized. Please log in again.';
    } else {
      errorMessage.value = 'Failed to submit the comment. Please try again later.';
    }
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<template>
  <div class="comment-form">
    <textarea
      v-model="commentContent"
      placeholder="Write your comment..."
      rows="3"
      maxlength="500"
    ></textarea>

    <!-- Disable the button if user is not logged in or while submitting -->
    <button :disabled="commentContent.length == 0 || isSubmitting || !isLoggedIn" @click="submitComment">
      {{ isSubmitting ? 'Submitting...' : isLoggedIn ? 'Post Comment' : 'Login to Comment' }}
    </button>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    <p v-if="!isLoggedIn" class="login-message">Please log in to post a comment.</p>
  </div>
</template>

<style scoped>
.comment-form {
  margin-top: 20px;
}

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-size: 1rem;
  resize: none;
}

button {
  margin-top: 10px;
  padding: 10px 20px;
  font-size: 1rem;
  color: #fff;
  background-color: #007bff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  margin-top: 10px;
  color: red;
  font-size: 0.9rem;
}

.login-message {
  margin-top: 10px;
  color: #f44336; /* Red color for the login message */
  font-size: 0.9rem;
}
</style>
