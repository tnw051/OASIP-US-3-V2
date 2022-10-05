<script setup>
import { formatDateAndFromToTime } from "../utils";
import Badge from "./Badge.vue";

const props = defineProps({
  currentEvent: {
    type: Object,
    default: () => ({}),
  },
});

const emits = defineEmits([
  "close",
]);
</script>
 
<template>
  <div class=" bg-white p-6 rounded-2xl flex flex-col gap-3 shadow-xl border-b-2 border-white/50 shadow-black/5 break-words w-full">
    <!-- close button -->
    <div
      class="absolute top-1 right-1 mt-1 mr-1 cursor-pointer text-gray-500 hover:bg-gray-50 rounded-full w-10 h-10 transition flex justify-center items-center font-bold"
      @click="$emit('close')"
    >
      ⨉
    </div>
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
      class="self-baseline mb-2"
    />

    <div>
      <p>{{ formatDateAndFromToTime(props.currentEvent.eventStartTime, props.currentEvent.eventDuration) }}</p>
      <p class="text-gray-500 text-sm">
        {{ props.currentEvent.eventDuration }} {{ props.currentEvent.eventDuration > 1 ?
          'minutes' : 'minute'
        }}
      </p>
    </div>

    <div>
      <p class="text-gray-500 text-sm">
        Notes
      </p>
      <p>{{ props.currentEvent.eventNotes || '–' }}</p>
    </div>
  </div>
</template>
 
<style scoped>
</style>