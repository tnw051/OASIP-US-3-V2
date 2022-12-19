<script setup lang="ts">
import { toFormValidator } from "@vee-validate/zod";
import { AxiosError } from "axios";
import { useForm } from "vee-validate";
import { computed, onBeforeMount, ref, watchEffect } from "vue";
import { z } from "zod";
import { useAuthStore } from "../auth/useAuthStore";
import FileUploader from "../components/form/FileUploader.vue";
import InputField from "../components/form/InputField.vue";
import { useFile } from "../composables/useFile";
import { useOverlapValidator } from "../composables/useOverlapValidator";
import { CategoryResponse } from "../gen-types";
import { createEvent, getCategories } from "../service/api";
import { ErrorResponse } from "../types";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import BaseModal from "./BaseModal.vue";

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(["close", "success", "error"]);

const { isAuthenticated, isStudent, user } = useAuthStore();

const categories = ref<CategoryResponse[]>([]);
const eventValidationSchema = toFormValidator(
  z.object({
    bookingName: z.string().min(1, "Name is required").max(100, "Name exceeds 100 characters"),
    bookingEmail: z.string().email("Email is invalid").max(50, "Email exceeds 50 characters"),
    eventNotes: z.string().max(500, "Notes exceed 500 characters").optional(),
    eventStartTime: z.string().refine((value) => {
      const date = new Date(value);
      const now = new Date();
      return date.getTime() > now.getTime();
    }, "Start time must be in the future"),
    eventCategoryId: z.number(),
  }),
);

const { handleSubmit, setErrors, errors, resetForm, setFieldValue, values } = useForm({
  validationSchema: eventValidationSchema,
});

const hasErrors = computed(() => {
  return Object.keys(errors.value).length > 0;
});

const canSubmit = computed(() => {
  return !hasErrors.value &&
    values.bookingName !== undefined &&
    values.bookingEmail !== undefined &&
    values.eventStartTime !== undefined &&
    values.eventCategoryId !== undefined &&
    !isOverlapping.value;
});

const { isOverlapping, setEventCategory, setStartTime } = useOverlapValidator();

function onCategoryChange(categoryId: number) {
  const category = categories.value.find((c) => c.id === categoryId);
  setEventCategory(categoryId, category?.eventDuration);
  console.log(category, category?.eventDuration);

}

watchEffect(() => {
  preFillInputs();
});

function preFillInputs() {
  if (isAuthenticated.value && isStudent.value && user.value) {
    setFieldValue("bookingEmail", user.value.email);
  }
}

onBeforeMount(async () => {
  categories.value = await getCategories();
});

// format: 2022-02-02T02:02
const minDateTimeLocal = formatDateTimeLocal(new Date());

const { file, fileError, handleChangeFile, handleRemoveFile } = useFile();

const onSubmit = handleSubmit(async (inputs) => {
  try {
    const createdEvent = await createEvent({
      bookingName: inputs.bookingName,
      bookingEmail: inputs.bookingEmail,
      // convert local time to UTC in ISO-8601 format
      eventCategoryId: Number(inputs.eventCategoryId),
      eventStartTime: new Date(inputs.eventStartTime).toISOString(),
    }, file.value);

    if (createdEvent) {
      resetForm();
      handleRemoveFile();
      emit("success", createdEvent);
      return;
    }
  } catch (error) {
    if (error instanceof AxiosError) {
      const errorResponse = error.response?.data as ErrorResponse;
      if (error.response?.status === 400) {
        setErrors({
          bookingName: errorResponse.errors?.bookingName?.[0],
          bookingEmail: errorResponse.errors?.bookingEmail?.[0],
          eventStartTime: errorResponse.errors?.eventStartTime?.[0],
        });
      }
    }
  }

  emit("error");
});
</script>
 
<template>
  <BaseModal :is-open="isOpen">
    <div
      class="-translate-y-8"
    >
      <form
        class="m-auto flex max-w-lg flex-col gap-4 rounded-xl border border-gray-100 bg-white py-10 px-8 shadow-xl shadow-black/5"
        @submit.prevent="onSubmit"
      >
        <div class="mb-4 flex flex-col text-center text-gray-700">
          <h1 class="text-2xl font-medium">
            Create Event
          </h1>
        </div>

        <div class="flex flex-col gap-2">
          <div class="flex flex-col gap-1">
            <InputField
              label="Booking Name"
              name="bookingName"
              :required="true"
              placeholder="What's your booking name?"
            />
          </div>

          <div class="flex flex-col gap-1">
            <InputField
              label="Booking Email"
              name="bookingEmail"
              :required="!isStudent"
              placeholder="What's your booking email?"
              :disabled="isStudent"
              :class="{
                'border-none bg-[#ffffff]': isStudent,
              }"
            />
          </div>
        </div>

        <div class="flex gap-2">
          <InputField
            label="Start Time"
            name="eventStartTime"
            type="datetime-local"
            :min="minDateTimeLocal"
            :max="inputConstraits.MAX_DATETIME_LOCAL"
            :required="true"
            :error-message="isOverlapping && 'The start time is overlapping with another event'"
            @change="setStartTime($event.target.value)"
          />

          <InputField
            label="Category"
            name="eventCategoryId"
            as="select"
            :required="true"
            :use-field-slot="true"
            @change="onCategoryChange(Number($event.target.value))"
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
          </InputField>
        </div>

        <div class="flex flex-col gap-2">
          <InputField
            label="Notes"
            name="eventNotes"
            as="textarea"
            placeholder="What's your event about?"
          />
        </div>

        <div class="flex flex-col gap-2">
          <FileUploader
            :file-name="file?.name"
            :file-error="fileError"
            @change="handleChangeFile"
            @remove="handleRemoveFile"
          />

          <div class="flex justify-end gap-2">
            <button
              type="button"
              class="mt-2 rounded bg-gray-500 py-2 px-4 font-medium text-white hover:bg-gray-600"
              @click="emit('close')"
            >
              Cancel
            </button>
            <button
              type="submit"
              class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="!canSubmit"
            >
              Create
              Event
            </button>
          </div>
        </div>
      </form>
    </div>
  </BaseModal>
</template>
 
<style scoped>

</style>