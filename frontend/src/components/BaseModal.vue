<script setup>
const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
});

defineEmits(["close"]);
</script>
 
<template>
  <Teleport to="body">
    <div
      v-if="props.isOpen"
      class="absolute top-0 left-0 z-50 grid h-screen w-screen place-content-center bg-black/50 text-slate-700 shadow-2xl"
      @click="$emit('close')"
    >
      <slot />
    </div>
  </Teleport>
</template>
 
<style scoped>
</style>