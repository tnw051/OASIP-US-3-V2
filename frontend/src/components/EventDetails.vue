<script setup lang="ts">
import { EventResponse } from "../gen-types";
import { getDownloadUrl } from "../service/api";
import { formatDateAndFromToTime } from "../utils";
import Badge from "./Badge.vue";

interface Props {
  currentEvent: EventResponse;
}

const props = defineProps<Props>();

const emits = defineEmits([
  "close",
]);
</script>
 
<template>
  <div
    class="min-w-sm flex max-w-sm flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5"
  >
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

    <!-- <div>
      <p class="text-sm text-gray-500">
        Bucket UUID
      </p>
      <p>{{ props.currentEvent.bucketUuid }}</p>
    </div> -->

    <div
      v-for="file in props.currentEvent.files"
      :key="file.name"
    >
      <p class="text-sm text-gray-500">
        Files
      </p>
      <a
        :href="getDownloadUrl(file.bucketId, file.name)"
        target="_blank"
        rel="noopener noreferrer"
        class="text-blue-500 hover:underline"
      >
        {{ file.name }}
      </a>
    </div>
  </div>
</template>
 
<style scoped>

</style>