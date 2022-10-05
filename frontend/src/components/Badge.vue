<script setup>
const props = defineProps({
  text: {
    type: String,
    required: true,
  },
});
</script>
 
<template>
  <div class="text-sm text-sky-600 font-medium truncate rounded-md bg-sky-50 px-2">
    {{ props.text }}
  </div>
</template>
 
<style scoped>
</style>