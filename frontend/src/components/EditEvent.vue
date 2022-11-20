<script setup lang="ts">
import { EditEventRequest, EventResponse } from "../gen-types";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import { useEventValidator } from "../utils/useEventValidator";
import { useFileInput } from "../utils/useFileInput";
import Badge from "./Badge.vue";

interface Props {
  currentEvent: EventResponse;
}

const props = defineProps<Props>();

const emits = defineEmits([
  "save",
  "cancel",
]);

const minDateTImeLocal = formatDateTimeLocal(new Date());

const {
  errors,
  inputs,
  canSubmit,
} = useEventValidator({
  currentEvent: props.currentEvent,
});

function handleSaveClick() {
  const updates: EditEventRequest = {};

  const newDate = new Date(inputs.eventStartTime);
  if (newDate.getTime() !== new Date(props.currentEvent.eventStartTime).getTime()) {
    updates.eventStartTime = newDate.toISOString();
  }

  const newNotes = inputs.eventNotes;
  if (newNotes !== props.currentEvent.eventNotes) {
    updates.eventNotes = newNotes;
  }

  assertNoPlaceholder();
  emits("save", updates, file.value);
}

const { file, fileError, fileInputRef, handleBlurFileInput, handleFileChange, handleRemoveFile, assertNoPlaceholder, setPlaceholderWithName } = useFileInput();
</script>
 
<template>
  <div
    class=" flex w-full flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5"
  >
    <div>
      <p class="text-xl">
        {{ props.currentEvent.bookingName }}
      </p>
      <p class="text-gray-500">
        {{ props.currentEvent.bookingEmail }}
      </p>
    </div>

    <Badge
      :text="props.currentEvent.eventCategory.eventCategoryName"
      class="mb-2 self-baseline"
    />

    <p class="text-sm text-gray-500">
      {{ props.currentEvent.eventDuration }} {{ props.currentEvent.eventDuration > 1 ?
        'minutes' : 'minute'
      }}
    </p>

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
        v-if="errors.eventStartTime"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.eventStartTime"
          :key="error"
        >{{ error }}</span>
      </div>
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
        >
          {{ error }}
        </span>
      </div>
    </div>

    <!-- file -->
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
    </div>

    <div class="flex gap-2">
      <button
        class="mt-2 flex-1 rounded bg-gray-500 py-2 px-4 font-medium text-white hover:bg-gray-600"
        @click="$emit('cancel')"
      >
        Cancel
      </button>

      <button
        type="submit"
        class="mt-2 flex-1 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="!canSubmit"
        @click="handleSaveClick"
      >
        Save
      </button>
    </div>
  </div>
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>