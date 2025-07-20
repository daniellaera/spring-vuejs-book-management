<script setup lang="ts">
import {computed, ref, watch} from "vue";
import DatePicker from "primevue/datepicker";
import { useToast } from "primevue/usetoast";
import apiClient from "@/plugins/axiosConfig";
import Button from 'primevue/button';
import Message from "primevue/message";
import {isLoggedIn} from "@/service/useSession";

const props = defineProps<{
  minDate: Date | undefined;
  maxDate: Date | undefined;
  bookId: number
}>();

const selectedDates = ref<[Date | null, Date | null] | null>(null); // Selected range
const toast = useToast();
const emit = defineEmits(["borrow-range-updated"]);

// Emit for parent updates (if needed)
watch(selectedDates, (newDates) => {
  emit("borrow-range-updated", newDates);
});

const areDatesValid = computed(() => {
  return !selectedDates.value || !selectedDates.value[0] || !selectedDates.value[1]
});

const submitBorrowRequest = async () => {
  if (!selectedDates.value || !selectedDates.value[0] || !selectedDates.value[1]) {
    toast.add({
      severity: "warn",
      summary: "Invalid Input",
      detail: "Please select a valid date range.",
      life: 3000,
    });
    return;
  }

  try {
    const [startDate, endDate] = selectedDates.value;

    const payload = {
      borrowStartDate: startDate.toISOString(),
      borrowEndDate: endDate.toISOString(),
    };

    const token = localStorage.getItem("auth_token");

    await apiClient.post(`/borrow/${props.bookId}`, payload, {
      headers: { Authorization: `Bearer ${token}` },
    });

    toast.add({
      severity: "success",
      summary: "Success",
      detail: "Borrow request submitted successfully!",
      life: 3000,
    });

    // Notify parent component of the borrow range update
    emit("borrow-range-updated", [startDate, endDate]);
    selectedDates.value = null; // Optionally reset dates

  } catch (error) {
    console.error("Error submitting borrow request:", error);

    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to submit borrow request.",
      life: 3000,
    });
  }
};
</script>

<template>
  <div class="borrow-book">
    <h3>Select Borrowing Date Range</h3>
    <DatePicker
      v-model="selectedDates"
      selectionMode="range"
      format="dd/mm/yy"
      inline
      showWeek
      class="w-full sm:w-[30rem]"
      placeholder="Select Borrowing Dates"
      :manualInput="false"
      :minDate="props.minDate"
      :maxDate="props.maxDate"
    />
    <Button
      class="submit-button"
      @click="submitBorrowRequest"
      :disabled="!isLoggedIn || areDatesValid"
    >
      Submit Borrow Request
    </Button>
    <div v-if="!isLoggedIn" class="warn-message-container">
      <Message severity="warn">
        You must be logged in to create a borrow request
      </Message>
    </div>
  </div>
</template>

<style scoped>
.borrow-book {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 1rem 0;
}

.borrow-book h3 {
  margin-bottom: 1rem;
}

.submit-button {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-button:hover {
  background-color: #0056b3;
}

.warn-message-container {
  margin-top: 1rem;
  padding: 0.5rem;
  width: 100%; /* Optional: to center align the message */
}

</style>
