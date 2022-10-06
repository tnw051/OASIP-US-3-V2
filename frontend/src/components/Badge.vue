<script setup>
const props = defineProps({
  text: {
    type: String,
    required: true,
  },
});
</script>
 
<template>
  <div class=" truncate rounded-md bg-sky-50 px-2 text-sm font-medium text-sky-600">
    {{ props.text }}
  </div>
</template>
 
<style scoped>
</style>