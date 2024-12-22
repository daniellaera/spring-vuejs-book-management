<template>
  <div v-if="canRate" class="rating-form">
    <div class="form-group">
      <!-- Pass the stars prop as a number -->
      <Rating v-model="rating" :stars="5" />
      <Message v-if="errorMessage" severity="error" size="small" variant="simple">
        {{ errorMessage }}
      </Message>
    </div>
    <Button
      :disabled="!rating"
      @click="submitRating"
      label="Submit Rating"
      class="submit-button"
    />
  </div>
  <p v-else class="info-message">You have already rated this book.</p>
  <Toast />
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { isLoggedIn } from "@/service/useSession";
import apiClient from "@/plugins/axiosConfig";
import { useToast } from "primevue/usetoast";
import { Rating, Button, Message } from "primevue";
import type { RatingDTO } from "@/model/rating";
import Toast from "primevue/toast";

// Props for BookDTO data
const props = defineProps({
  sessionUserId: {
    type: Number,
    required: true,
  },
  bookId: {
    type: Number,
    required: true,
  },
  bookRatings: {
    type: Array as () => RatingDTO[] | undefined | null,
    required: true,
  },
});

// Emit event for parent to handle updates
const emit = defineEmits(["rating-added"]);

// Reactive states
const rating = ref<number | undefined>(undefined);
const isSubmitting = ref(false);
const errorMessage = ref<string | null>(null);

const toast = useToast();

// Allow users to rate only if they haven’t already
const canRate = computed(() => {
  return (
    !isLoggedIn.value ||
    !props.bookRatings?.some((rating) => rating.userId === props.sessionUserId)
  );
});

// Function to submit a rating
const submitRating = async () => {
  if (rating.value === null) {
    errorMessage.value = "Please select a rating before submitting.";
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = null;

  try {
    const token = localStorage.getItem("auth_token");

    const response = await apiClient.post(
      `/rating/${props.bookId}`,
      { score: rating.value },
      {
        headers: { Authorization: `Bearer ${token}` },
      }
    );

    emit("rating-added", response.data);

    rating.value = undefined;

    toast.add({ severity: "success", summary: "Rating submitted!", life: 3000 });
  } catch (error: any) {
    console.error("Error submitting rating:", error);
    errorMessage.value =
      error.response?.status === 401
        ? "Unauthorized. Please log in again."
        : "Failed to submit the rating. Please try again.";
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.rating-form {
  margin-top: 20px;
}

.submit-button {
  margin-top: 10px;
}

.info-message {
  color: #666;
  margin-top: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
