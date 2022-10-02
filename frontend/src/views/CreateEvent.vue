<script setup>
import { onBeforeMount, ref } from "vue";
import Modal from "../components/Modal.vue";
import { createEvent, getCategories } from "../service/api";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import { useAuth } from "../utils/useAuth";
import { useEventValidator } from "../utils/useEventValidator";

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
  canSubmit
} = useEventValidator();

const { isAuthenticated, user, isAdmin } = useAuth();
if (isAuthenticated.value && !isAdmin.value) {
  inputs.value.bookingEmail = user.value.sub;
}

onBeforeMount(async () => {
  categories.value = await getCategories();
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
    const createdEvent = await createEvent(event);

    if (createdEvent) {
      resetInputs();
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
</script>
 
<template>
  <div class="max-w-md mx-auto mt-8">
    <form @submit.prevent="handleSubmit"
      class="flex flex-col gap-4 bg-white py-10 px-8 border border-gray-100 rounded-xl shadow-xl shadow-black/5">

      <div class="flex flex-col text-center mb-4 text-gray-700">
        <h1 class="font-medium text-2xl">Create Event</h1>
      </div>

      <div class="flex flex-col gap-2">
        <label for="name" class="required text-sm font-medium text-gray-700">Booking Name</label>
        <input id="name" type="text" v-model="inputs.bookingName" required class="bg-gray-100 p-2 rounded"
          @input="validateBookingName" placeholder="What's your booking name?">
        <div v-if="errors.bookingName.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.bookingName">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="email" class="text-sm font-medium text-gray-700" :class="{ 'required': !isAuthenticated || isAdmin }">Booking Email</label>
        <!-- <span v-if="isAuthenticated" id="email" type="email" :value="user.sub" class="p-2 rounded"
          @input="validateBookingEmail" placeholder="What's your email?"> -->
        <span v-if="isAuthenticated && !isAdmin" class="p-2 rounded" :value="user.sub">{{ user.sub }}</span>
        <input v-else id="email" type="email" v-model="inputs.bookingEmail" required class="bg-gray-100 p-2 rounded"
          @input="validateBookingEmail" placeholder="What's your email?">
        <div v-if="errors.bookingEmail.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.bookingEmail">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="startTime" class="required text-sm font-medium text-gray-700">Start Time</label>
        <input id="startTime" type="datetime-local" :min="minDateTImeLocal" :max="inputConstraits.MAX_DATETIME_LOCAL"
          v-model="inputs.eventStartTime" required class="bg-gray-100 p-2 rounded" @input="validateStartTime">
        <div v-if="errors.eventStartTime.length > 0 || errors.hasOverlappingEvents"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.eventStartTime">{{ error }}</span>
          <span v-if="errors.hasOverlappingEvents">Start time overlaps with other event(s)</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="category" class="required text-sm font-medium text-gray-700">Category</label>
        <select v-model="inputs.eventCategoryId" required class="bg-gray-100 p-2 rounded"
          @change="handleCategoryIdChange" id="category">
          <option disabled selected value="">Select event category</option>
          <option v-for="category in categories" :value="category.id">{{ category.eventCategoryName }} ({{
              category.eventDuration
          }} min.)</option>
        </select>
      </div>

      <div class="flex flex-col gap-2">
        <label for="notes" class="text-sm font-medium text-gray-700">Notes <span
            class="text-gray-400 font-normal">(optional)</span></label>
        <textarea id="notes" v-model="inputs.eventNotes" class="bg-gray-100 p-2 rounded" @input="validateEventNotes"
          placeholder="What's your event about?"></textarea>
        <div v-if="errors.eventNotes.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.eventNotes">{{ error }}</span>
        </div>
      </div>

      <button type="submit"
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded disabled:opacity-60 disabled:cursor-not-allowed mt-2"
        :disabled="!canSubmit">Create
        Event</button>
    </form>
  </div>

  <Modal title="Success" subtitle="Event created successfully" :is-open="isSuccessModalOpen"
    @close="isSuccessModalOpen = false" />

  <Modal title="Error" subtitle="Something went wrong" button-text="Try Again" :is-open="isErrorModalOpen"
    variant="error" @close="isErrorModalOpen = false" />
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>