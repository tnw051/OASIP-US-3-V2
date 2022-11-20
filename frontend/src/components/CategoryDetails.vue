<script setup lang="ts">
import { CategoryResponse } from "../gen-types";

interface Props {
  currentCategory: CategoryResponse;
}

const props = defineProps<Props>();

const emits = defineEmits(["close"]);
</script>

<template>
  <div
    class="flex w-full flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5"
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
        {{ props.currentCategory.eventCategoryName }}
      </p>
      <p class="text-gray-500">
        {{ props.currentCategory.eventCategoryDescription }}
      </p>
      <p class="text-gray-500">
        {{ props.currentCategory.eventDuration }}
      </p>
    </div>
  </div>
</template>

<style>

</style>
