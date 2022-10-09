<script setup lang="ts">
import { EventResponse } from "../gen-types";
import { formatDateAndFromToTime } from "../utils";
import Badge from "./Badge.vue";

interface Props {
  currentEvent: EventResponse | Record<string, never>;
}

const props = defineProps<Props>();

const emits = defineEmits([
  "close",
]);
</script>
 
<template>
  <div class=" flex w-full flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5">
    <!-- close button -->
    <div
      class="absolute top-1 right-1 mt-1 mr-1 flex h-10 w-10 cursor-pointer items-center justify-center rounded-full font-bold text-gray-500 transition hover:bg-gray-50"
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
      class="mb-2 self-baseline"
    />

    <div>
      <p>{{ formatDateAndFromToTime(props.currentEvent.eventStartTime, props.currentEvent.eventDuration) }}</p>
      <p class="text-sm text-gray-500">
        {{ props.currentEvent.eventDuration }} {{ props.currentEvent.eventDuration > 1 ?
          'minutes' : 'minute'
        }}
      </p>
    </div>

    <div>
      <p class="text-sm text-gray-500">
        Notes
      </p>
      <p>{{ props.currentEvent.eventNotes || '–' }}</p>
    </div>
  </div>
</template>
 
<style scoped>
</style>