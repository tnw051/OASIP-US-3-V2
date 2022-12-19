<script setup>
const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  required: {
    type: Boolean,
    required: true,
  },
});
</script>
 
<template>
  <label
    class="text-sm font-medium text-slate-500"
    :class="{ 'required': required }"
    :for="props.name"
  >
    {{ label }} 
    <span
      v-if="!required"
      class="font-normal text-gray-400"
    >(optional)</span>
  </label>
</template>
 
<style scoped>

</style>