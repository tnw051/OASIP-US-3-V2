<script setup lang="ts">
import { EventResponse } from "../gen-types";
import { formatDateAndFromToTime } from "../utils";
import Badge from "./Badge.vue";
import { getFilenameByBucketUuid, getBucketURL } from "../service/api";
import { onBeforeMount, ref, watch } from "vue";

interface Props {
  currentEvent: EventResponse;
}

const props = defineProps<Props>();

const emits = defineEmits([
  "close",
]);

// fetch image using url from props.currentEvent.bucketUuid
async function getFilename() {
  const bucketUuid = props.currentEvent.bucketUuid;
  if (!bucketUuid) {
    return;
  }
  const file = await getFilenameByBucketUuid(bucketUuid);
  return file;
}

const filename = ref();
const isLoading = ref(true);

getFilename().then((res) => {
  filename.value = res;
  isLoading.value = false;
});

watch(
  () => props.currentEvent.bucketUuid,
  async (newVal, oldVal) => {
    if (newVal !== oldVal) {
      isLoading.value = true;
      filename.value = await getFilename();
      isLoading.value = false;
    }
  },
);
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

    <div v-if="props.currentEvent.bucketUuid">
      <p class="text-sm text-gray-500">
        Files
      </p>
      <a
        v-if="!isLoading"
        :href="getBucketURL(props.currentEvent.bucketUuid)"
        target="_blank"
        rel="noopener noreferrer"
        class="text-blue-500 hover:underline"
      >
        {{ filename }}
      </a>
      <p
        v-else
        class="text-gray-500"
      >
        Loading...
      </p>
    </div>
  </div>
</template>
 
<style scoped>

</style>