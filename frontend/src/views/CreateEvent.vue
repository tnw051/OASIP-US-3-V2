<script setup lang="ts">
import { onBeforeMount, ref } from "vue";
import Modal from "../components/Modal.vue";
import { CategoryResponse } from "../gen-types";
import { createEvent, getCategories } from "../service/api";
import { ErrorResponse } from "../types";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import { useAuth } from "../utils/useAuth";
import { useEventValidator } from "../utils/useEventValidator";

const categories = ref<CategoryResponse[]>([]);
const {
  errors,
  inputs,
  resetInputsAndErrors,
  hasErrors,  
} = useEventValidator({
  getDurationByCategoryId(categoryId) {
    const category = categories.value.find((c) => c.id === categoryId);
    return category ? category.eventDuration : null;
  },
});

const { isAuthenticated, user, isAdmin } = useAuth();
function preFillInputs() {
  if (isAuthenticated.value && !isAdmin.value) {
    if (!user.value) {
      console.log("User is not loaded");
    } else {
      inputs.bookingEmail = user.value.sub;
    }
  }
}

onBeforeMount(async () => {
  preFillInputs();
  categories.value = await getCategories();
});

// format: 2022-02-02T02:02
const minDateTImeLocal = formatDateTimeLocal(new Date());

const isSuccessModalOpen = ref(false);
const isErrorModalOpen = ref(false);

async function handleSubmit() {
  if (!inputs.bookingEmail || !inputs.bookingName || !inputs.eventCategoryId || !inputs.eventNotes || !inputs.eventStartTime) {
    console.log("Missing required fields");
    return;
  }

  try {
    const createdEvent = await createEvent({
      bookingName: inputs.bookingName,
      bookingEmail: inputs.bookingEmail,
      // convert local time to UTC in ISO-8601 format
      eventCategoryId: Number(inputs.eventCategoryId),
      eventStartTime: new Date(inputs.eventStartTime).toISOString(),
    }, file.value ? file.value : null);

    if (createdEvent) {
      resetInputsAndErrors();
      preFillInputs();
      isSuccessModalOpen.value = true;
    } else {
      isErrorModalOpen.value = true;
    }
  } catch (errorResponse) {
    const error = errorResponse as ErrorResponse;
    if (error.status !== 400) {
      isErrorModalOpen.value = true;
      return;
    }

    Object.assign(errors, error.errors);
  }
}

// file attachment
const file = ref<File>();
const fileError = ref<string[] | false>(false);
const fileInputRef = ref<HTMLInputElement | null>(null);

const maxFileSize = 10 * 1024 * 1024;

function handleFileChange(e: Event) {
  console.log("handleFileChange");
  
  const target = e.target as HTMLInputElement;
  const files = target.files;
  const selectedFile = files && files[0];

  // user cancelled file selection
  if (!selectedFile) {
    // make sure to clear the previous selected file if any
    handleRemoveFile();
    if (fileError.value) {
      fileError.value = false;
    }
    return;
  }

  if (selectedFile.size > maxFileSize) {
    // if there is no file selected before, clear the file input.
    fileError.value = [`${selectedFile.name} is too large.`, `Maximum file size is ${maxFileSize / 1024 / 1024} MB.`];
    if (!file.value) {
      handleRemoveFile();
    } else {
      // otherwise, keep the previous file selected
      const newFileList = new DataTransfer();
      const prevFile = file.value;
      newFileList.items.add(prevFile);
      if(fileInputRef.value?.files) {
        fileInputRef.value.files = newFileList.files;
      }
      fileError.value.push(`The previous file '${prevFile.name}' is still selected.`);
    }

    return;
  }

  file.value = selectedFile;
  fileError.value = false;
}

function handleBlurFileInput() {
  if (fileError.value) {
    fileError.value = false;
  }
}

function handleRemoveFile() {
  file.value = undefined;
  if (fileInputRef.value) {
    fileInputRef.value.value = "";
  }
}
</script>
 
<template>
  <div class="mx-auto mt-8 max-w-md">
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
        >
        <div
          v-if="errors.bookingName"
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
        <span
          v-if="isAuthenticated && !isAdmin"
          class="rounded p-2"
          :value="user?.sub"
        >{{ user?.sub }}</span>
        <input
          v-else
          id="email"
          v-model="inputs.bookingEmail"
          type="email"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your email?"
        >
        <div
          v-if="errors.bookingEmail"
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
        >
        <div
          v-if="errors.eventStartTime || errors.hasOverlappingEvents"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.eventStartTime"
            :key="error.toString()"
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
        />
        <div
          v-if="errors.eventNotes"
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
          :disabled="hasErrors"
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