<script setup lang="ts">
import { onBeforeMount, ref, watchEffect } from "vue";
import { useAuthStore } from "../auth/useAuthStore";
import Modal from "../components/Modal.vue";
import { createEvent, getCategories } from "../service/api";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import { useEventValidator } from "../utils/useEventValidator";
import { useFileInput } from "../utils/useFileInput";
import { useIsLoading } from "../utils/useIsLoading";

const { isAuthenticated, isAdmin, user } = useAuthStore();

const { isLoading, setIsLoading } = useIsLoading(true);

const categories = ref([]);
const {
  errors,
  inputs,
  validateBookingName,
  validateBookingEmail,
  validateEventNotes,
  validateStartTime,
  validateCategoryId,
  setEventDuration,
  resetInputs,
  canSubmit,
} = useEventValidator();

watchEffect(() => {
  preFillInputs();
});

function preFillInputs() {
  if (isAuthenticated.value && !isAdmin.value) {
    inputs.value.bookingEmail = user.value.email;
  }
}

onBeforeMount(async () => {
  preFillInputs();
  categories.value = await getCategories();
  setIsLoading(false);
});

// format: 2022-02-02T02:02
const minDateTImeLocal = formatDateTimeLocal(new Date());

const isSuccessModalOpen = ref(false);
const isErrorModalOpen = ref(false);

async function handleSubmit() {
  const event = {
    ...inputs.value,

    // convert local time to UTC in ISO-8601 format
    eventStartTime: new Date(inputs.value.eventStartTime).toISOString(),
  };

  try {
    const createdEvent = await createEvent({
      bookingEmail: event.bookingEmail,
      bookingName: event.bookingName,
      eventCategoryId: Number(event.eventCategoryId),
      eventNotes: event.eventNotes,
      eventStartTime: event.eventStartTime,
    }, file.value ? file.value : null);

    if (createdEvent) {
      resetInputs();
      preFillInputs();
      isSuccessModalOpen.value = true;
    } else {
      isErrorModalOpen.value = true;
    }
  } catch (errorResponse) {
    if (errorResponse.status !== 400) {
      isErrorModalOpen.value = true;
      return;
    }

    Object.assign(errors.value, errorResponse.errors);
  }
}

function handleCategoryIdChange() {
  const eventCategoryId = inputs.value.eventCategoryId;
  const category = categories.value.find((category) => category.id === eventCategoryId);
  setEventDuration(category.eventDuration);
  validateCategoryId();
}


// file attachment
const { file, fileError, fileInputRef, handleBlurFileInput, handleFileChange, handleRemoveFile } = useFileInput();
</script>
 
<template>
  <div
    v-if="!isLoading"
    class="mx-auto mt-8 max-w-md"
  >
    <form
      class="flex flex-col gap-4 rounded-xl border border-gray-100 bg-white py-10 px-8 shadow-xl shadow-black/5"
      @submit.prevent="handleSubmit"
    >
      <div class="mb-4 flex flex-col text-center text-gray-700">
        <h1 class="text-2xl font-medium">
          Create Event
        </h1>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="name"
          class="required text-sm font-medium text-gray-700"
        >Booking Name</label>
        <input
          id="name"
          v-model="inputs.bookingName"
          type="text"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your booking name?"
          @input="validateBookingName"
        >
        <div
          v-if="errors.bookingName.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.bookingName"
            :key="error"
          >{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="email"
          class="text-sm font-medium text-gray-700"
          :class="{ 'required': !isAuthenticated || isAdmin }"
        >Booking Email</label>
        <!-- <span v-if="isAuthenticated" id="email" type="email" :value="user.sub" class="p-2 rounded"
          @input="validateBookingEmail" placeholder="What's your email?"> -->
        <span
          v-if="isAuthenticated && !isAdmin"
          class="rounded p-2"
        >{{ inputs.bookingEmail }}</span>
        <input
          v-else
          id="email"
          v-model="inputs.bookingEmail"
          type="email"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your email?"
          @input="validateBookingEmail"
        >
        <div
          v-if="errors.bookingEmail.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.bookingEmail"
            :key="error"
          >{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="startTime"
          class="required text-sm font-medium text-gray-700"
        >Start Time</label>
        <input
          id="startTime"
          v-model="inputs.eventStartTime"
          type="datetime-local"
          :min="minDateTImeLocal"
          :max="inputConstraits.MAX_DATETIME_LOCAL"
          required
          class="rounded bg-gray-100 p-2"
          @input="validateStartTime"
        >
        <div
          v-if="errors.eventStartTime.length > 0 || errors.hasOverlappingEvents"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.eventStartTime"
            :key="error"
          >{{ error }}</span>
          <span v-if="errors.hasOverlappingEvents">Start time overlaps with other event(s)</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="category"
          class="required text-sm font-medium text-gray-700"
        >Category</label>
        <select
          id="category"
          v-model="inputs.eventCategoryId"
          required
          class="rounded bg-gray-100 p-2"
          @change="handleCategoryIdChange"
        >
          <option
            disabled
            selected
            value=""
          >
            Select event category
          </option>
          <option
            v-for="category in categories"
            :key="category.id"
            :value="category.id"
          >
            {{ category.eventCategoryName }} ({{
              category.eventDuration
            }} min.)
          </option>
        </select>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="notes"
          class="text-sm font-medium text-gray-700"
        >Notes <span
          class="font-normal text-gray-400"
        >(optional)</span></label>
        <textarea
          id="notes"
          v-model="inputs.eventNotes"
          class="rounded bg-gray-100 p-2"
          placeholder="What's your event about?"
          @input="validateEventNotes"
        />
        <div
          v-if="errors.eventNotes.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.eventNotes"
            :key="error"
          >{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="file"
          class="text-sm font-medium text-gray-700"
        >File <span
          class="font-normal text-gray-400"
        >(optional)</span></label>
        <div class="flex items-center justify-between rounded bg-gray-100 p-2">
          <input
            id="file"
            ref="fileInputRef"
            type="file"
            @change="handleFileChange"
          >
          <!-- remove file button -->
          <button
            v-if="file"
            type="button"
            class="text-red-500"
            @click="handleRemoveFile"
            @blur="handleBlurFileInput"
          >
            <span class="material-symbols-outlined m-auto block">
              delete
            </span>
          </button>
        </div>
        <div
          v-if="fileError"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in fileError"
            :key="error"
          >{{ error }}</span>
        </div>

        <button
          type="submit"
          class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="!canSubmit"
        >
          Create
          Event
        </button>
      </div>
    </form>
  </div>

  <Modal
    title="Success"
    subtitle="Event created successfully"
    :is-open="isSuccessModalOpen"
    @close="isSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isErrorModalOpen"
    variant="error"
    @close="isErrorModalOpen = false"
  />
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>