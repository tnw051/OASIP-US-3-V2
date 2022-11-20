<script setup lang="ts">
import { AxiosError } from "axios";
import { ErrorMessage, Field, useIsFormDirty, useIsFormTouched } from "vee-validate";
import {
  onBeforeMount,
  onMounted,
  ref,
  watch,
  watchEffect,
} from "vue";
import { useAuthStore } from "../auth/useAuthStore";
import Modal from "../components/Modal.vue";
import { CategoryResponse } from "../gen-types";
import { createEvent, getCategories } from "../service/api";
import { ErrorResponse } from "../types";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import { useEventValidator } from "../utils/useEventValidatorNew";
import { useFileInput } from "../utils/useFileInput";
import { useIsLoading } from "../utils/useIsLoading";

const { isAuthenticated, isAdmin, user } = useAuthStore();

const categories = ref<CategoryResponse[]>([]);
const {
  handleSubmit,
  resetForm,
  setValues,
  setErrors, 
  values,
  errors,
  hasErrors,
  canSubmit,
} = useEventValidator({
  getDurationByCategoryId(categoryId) {
    const category = categories.value.find((c) => c.id === categoryId);
    return category ? category.eventDuration : null;
  },
});

watchEffect(() => {
  preFillInputs();
});

function preFillInputs() {
  if (isAuthenticated.value && !isAdmin.value && user.value) {
    setValues({
      bookingEmail: user.value.email,
    });
  }
}

onBeforeMount(async () => {
  categories.value = await getCategories();
});

// format: 2022-02-02T02:02
const minDateTimeLocal = formatDateTimeLocal(new Date());

const isSuccessModalOpen = ref(false);
const isErrorModalOpen = ref(false);

const onSubmit = handleSubmit(async (inputs) => {
  try {
    const createdEvent = await createEvent({
      bookingName: inputs.bookingName,
      bookingEmail: inputs.bookingEmail,
      // convert local time to UTC in ISO-8601 format
      eventCategoryId: Number(inputs.eventCategoryId),
      eventStartTime: new Date(inputs.eventStartTime).toISOString(),
    }, file.value ? file.value : null);

    if (createdEvent) {
      resetForm();
      isSuccessModalOpen.value = true;
    } else {
      isErrorModalOpen.value = true;
    }
  } catch (error) {
    if (error instanceof AxiosError) {
      const errorResponse = error.response?.data as ErrorResponse;
      if (error.response?.status !== 400) {
        isErrorModalOpen.value = true;
        return;
      }
      setErrors({
        bookingName: errorResponse.errors?.bookingName[0],
        bookingEmail: errorResponse.errors?.bookingEmail[0],
        eventStartTime: errorResponse.errors?.eventStartTime[0],
      });
    }
  }
});

// file attachment
const { file, fileError, fileInputRef, handleBlurFileInput, handleFileChange, handleRemoveFile } = useFileInput();
</script>
 
<template>
  <div
    class="mx-auto mt-8 max-w-md"
  >
    <form
      class="flex flex-col gap-4 rounded-xl border border-gray-100 bg-white py-10 px-8 shadow-xl shadow-black/5"
      @submit.prevent="onSubmit"
    >
      <div class="mb-4 flex flex-col text-center text-gray-700">
        <h1 class="text-2xl font-medium">
          Create Event
        </h1>
      </div>

      <div class="flex flex-col gap-1">
        <!-- Name -->
        <div class="flex flex-col gap-1">
          <label
            for="bookingName"
            class="required text-sm font-medium text-slate-500"
          >Booking Name</label>
          <Field
            id="bookingName"
            name="bookingName"
            class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
            placeholder="What's your booking name?"
          />
          <ErrorMessage
            name="bookingName"
            class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
          />
        </div>

        <!-- Email -->
        <div class="flex flex-col gap-1">
          <label
            for="bookingEmail"
            class="required text-sm font-medium text-slate-500"
          >Booking Email</label>
          <Field
            id="bookingEmail"
            name="bookingEmail"
            class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
            placeholder="What's your booking email?"
            :class="{
              'border-green-500': isAuthenticated && !isAdmin,
            }"
          />
          <ErrorMessage
            name="bookingEmail"
            class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
          />
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="eventStartTime"
          class="required text-sm font-medium text-slate-500"
        >Start Time</label>
        <Field
          id="eventStartTime"
          name="eventStartTime"
          type="datetime-local"
          :min="minDateTimeLocal"
          :max="inputConstraits.MAX_DATETIME_LOCAL"
          required
          class="rounded bg-gray-100 p-2"
        />
        <ErrorMessage
          name="eventStartTime"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="eventCategoryId"
          class="required text-sm font-medium text-slate-500"
        >Category</label>
        <Field
          id="eventCategoryId"
          name="eventCategoryId"
          as="select"
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
        </Field>
        <ErrorMessage
          name="eventCategoryId"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        />
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="eventNotes"
          class="text-sm font-medium text-slate-500"
        >Notes <span
          class="font-normal text-gray-400"
        >(optional)</span></label>
        <Field
          id="eventNotes"
          name="eventNotes"
          as="textarea"
          class="rounded bg-gray-100 p-2"
          placeholder="What's your event about?"
        />
        <ErrorMessage
          name="eventNotes"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        />
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
          class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="canSubmit"
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