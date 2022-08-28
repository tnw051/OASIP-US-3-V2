<script setup>
import { formatDateTimeLocal, inputConstraits } from '../utils';
import { useEventValidator } from '../utils/useEventValidator';
import Badge from './Badge.vue';

const props = defineProps({
  currentEvent: {
    type: Object,
    default: {},
  }
});

const emits = defineEmits([
  'save',
  'cancel'
]);

const minDateTImeLocal = formatDateTimeLocal(new Date());

const {
  errors,
  inputs,
  validateEventNotes,
  validateStartTime,
  setEventDuration,
  canSubmit,
  setEventId,
  setCategoryId
} = useEventValidator();


// only use three fields for now (including eventCategoryId)
inputs.value = {
  eventStartTime: formatDateTimeLocal(new Date(props.currentEvent.eventStartTime)),
  eventNotes: props.currentEvent.eventNotes
};

setEventId(props.currentEvent.id);
setEventDuration(props.currentEvent.eventDuration);
setCategoryId(props.currentEvent.eventCategory.id);

function handleSaveClick() {
  const updates = {};

  const newDate = new Date(inputs.value.eventStartTime);
  if (newDate.getTime() !== new Date(props.currentEvent.eventStartTime).getTime()) {
    updates.eventStartTime = newDate.toISOString();
  }

  const newNotes = inputs.value.eventNotes;
  if (newNotes !== props.currentEvent.eventNotes) {
    updates.eventNotes = newNotes;
  }

  emits('save', updates);
}
</script>
 
<template>
  <div
    class=" bg-white p-6 rounded-2xl flex flex-col gap-3 shadow-xl border-b-2 border-white/50 shadow-black/5 break-words w-full">
    <div>
      <p class="text-xl">{{ props.currentEvent.bookingName }}</p>
      <p class="text-gray-500">{{ props.currentEvent.bookingEmail }}</p>
    </div>

    <Badge :text="props.currentEvent.eventCategory.eventCategoryName" class="self-baseline mb-2" />

    <p class="text-gray-500 text-sm">{{ props.currentEvent.eventDuration }} {{ props.currentEvent.eventDuration > 1 ?
        'minutes' : 'minute'
    }}</p>

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
      <label for="notes" class="text-sm font-medium text-gray-700">Notes <span
          class="text-gray-400 font-normal">(optional)</span></label>
      <textarea id="notes" v-model="inputs.eventNotes" class="bg-gray-100 p-2 rounded" @input="validateEventNotes"
        placeholder="What's your event about?"></textarea>
      <div v-if="errors.eventNotes.length > 0"
        class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
        <span v-for="error in errors.eventNotes">{{ error }}</span>
      </div>
    </div>

    <div class="flex gap-2">
      <button class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-4 rounded mt-2 flex-1"
        @click="$emit('cancel')">Cancel</button>

      <button type="submit"
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded disabled:opacity-60 disabled:cursor-not-allowed mt-2 flex-1"
        @click="handleSaveClick" :disabled="!canSubmit">Save</button>

    </div>
  </div>
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>