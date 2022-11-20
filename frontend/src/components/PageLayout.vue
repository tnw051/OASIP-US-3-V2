<script setup lang="ts">
defineProps({
  header: {
    type: String,
    required: true,
  },
  subheader: {
    type: String,
    required: false,
    default: "",
  },
});
</script>
 
<template>
  <div
    class="page-layout mx-auto flex w-full max-w-7xl break-words rounded-2xl border-b-2 border-white/50 p-6"
  >
    <!-- Header -->
    <div class="text-4xl font-medium text-slate-800">
      {{ header }}
    </div>
    <div class="mt-2 text-slate-600">
      <slot
        v-if="subheader === ''"
        name="subheader"
      />
      <template v-else>
        {{ subheader }}
      </template>
    </div>
    <div>
      <slot
        name="content"
      >
        <div class="mt-8 w-full rounded-lg bg-slate-100 p-4 text-slate-500">
          Content
        </div>
      </slot>
    </div>
  </div>
</template>
 
<style scoped>
.page-layout {
  display: grid;
  grid-template-rows: auto auto 1fr;
  grid-template-areas:
    "header"
    "subheader"
    "content";
  height: 100%;
}

</style>