<script setup lang="ts">
import { toFormValidator } from "@vee-validate/zod";
import { useForm } from "vee-validate";
import { computed, PropType } from "vue";
import { z } from "zod";
import { useFile } from "../composables/useFile";
import { useOverlapValidator } from "../composables/useOverlapValidator";
import { EditEventRequest, EventResponse } from "../gen-types";
import { formatDateTimeLocal, inputConstraits } from "../utils";
import Badge from "./Badge.vue";
import FileUploader from "./form/FileUploader.vue";
import InputField from "./form/InputField.vue";

const props = defineProps({
  currentEvent: {
    type: Object as PropType<EventResponse>,
    required: true,
  },
});

const emits = defineEmits([
  "save",
  "cancel",
]);

const minDateTimeLocal = formatDateTimeLocal(new Date());

const eventValidationSchema = toFormValidator(
  z.object({
    eventNotes: z.string().max(500, "Notes exceed 500 characters").optional().nullable(),
    eventStartTime: z.string().refine((value) => {
      const date = new Date(value);
      const now = new Date();
      return date.getTime() > now.getTime();
    }, "Start time must be in the future"),
  }),
);

const { values, errors } = useForm({
  validationSchema: eventValidationSchema,
  initialValues: {
    eventNotes: props.currentEvent.eventNotes,
    eventStartTime: props.currentEvent.eventStartTime ? formatDateTimeLocal(new Date(props.currentEvent.eventStartTime)) : undefined,
  },
});

const hasErrors = computed(() => {
  return Object.keys(errors.value).length > 0;
});

const hasChanges = computed(() => {
  const currentEvent = props.currentEvent;
  return values.eventNotes !== currentEvent.eventNotes ||
    new Date(values.eventStartTime).getTime() !== new Date(currentEvent.eventStartTime).getTime() ||
    isDirty.value;
});

const canSubmit = computed(() => {
  return !hasErrors.value && hasChanges.value;
});

const startTime = new Date(props.currentEvent.eventStartTime);
const endTime = new Date(props.currentEvent.eventStartTime);
endTime.setMinutes(endTime.getMinutes() + props.currentEvent.eventDuration);

const { isOverlapping, setStartTime } = useOverlapValidator({
  currentTimeSlot: {
    eventCategoryId: props.currentEvent.eventCategory.id,
    eventDuration: props.currentEvent.eventDuration,
    eventStartTime: startTime,
    eventEndTime: endTime,
  },
});

const { file, fileError, handleChangeFile, handleRemoveFile, isDirty } = useFile();

function handleSaveClick() {
  const updates: EditEventRequest = {};

  const newDate = new Date(values.eventStartTime);
  if (!isNaN(newDate.getTime()) && newDate.getTime() !== new Date(props.currentEvent.eventStartTime).getTime()) {
    updates.eventStartTime = newDate.toISOString();
  }

  const newNotes = values.eventNotes;
  if (newNotes !== props.currentEvent.eventNotes) {
    updates.eventNotes = newNotes;
  }

  emits("save", updates, isDirty.value ? file.value : undefined);
}

const currentFilename = computed(() => {
  if (isDirty.value) {
    return file.value?.name;
  }

  if (props.currentEvent.files) {
    return props.currentEvent.files[0].name;
  }

  return undefined;
});
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
    </div>

    <div class="flex flex-col gap-2">
      <InputField
        label="Notes"
        name="eventNotes"
        as="textarea"
        placeholder="What's your event about?"
      />
    </div>

    <!-- file -->
    <div class="flex flex-col gap-2">
      <FileUploader
        :file-name="currentFilename"
        :file-error="fileError"
        @change="handleChangeFile"
        @remove="handleRemoveFile"
      />
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