<script setup>
const props = defineProps({
  currentCategory: {
    type: Object,
    default: () => ({}),
  },
});

const emits = defineEmits(["close"]);
</script>

<template>
  <div
    class="bg-white p-6 rounded-2xl flex flex-col gap-3 shadow-xl border-b-2 border-white/50 shadow-black/5 break-words w-full"
  >
    <!-- close button -->
    <div
      class="absolute top-1 right-1 mt-1 mr-1 cursor-pointer text-gray-500 hover:bg-gray-50 rounded-full w-10 h-10 transition flex justify-center items-center font-bold"
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
