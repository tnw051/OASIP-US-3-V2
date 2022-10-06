<script setup>
import { computed } from "vue";

const props = defineProps({
  variant: {
    type: String,
    default: "success",
  },
  title: {
    type: String,
    default: "Title",
  },
  subtitle: {
    type: String,
    default: "Subtitle",
  },
  buttonText: {
    type: String,
    default: "OK",
  },
  buttonConfirmText: {
    type: String,
    default: "Confirm",
  },
  buttonCancelText: {
    type: String,
    default: "Cancel",
  },
  isOpen: {
    type: Boolean,
    required: true,
  },
  type: {
    type: String,
    default: "close",
  },
});

const iconClass = computed(() => {
  if (props.variant === "success") {
    return "text-green-400 border-green-400";
  }
  if (props.variant === "error") {
    return "text-rose-400 border-rose-400";
  }

  return "";
});

const buttonClass = computed(() => {
  if (props.variant === "success") {
    return "bg-green-500 hover:bg-green-600";
  }
  if (props.variant === "error") {
    return "bg-rose-500 hover:bg-rose-600";
  }

  return "";
});

const emits = defineEmits(["close", "confirm"]);
</script>
 
<template>
  <Teleport to="body">
    <div
      v-if="props.isOpen"
      class="absolute inset-0 z-50 flex bg-black/50 text-slate-700 shadow-2xl"
      @click="$emit('close')"
    >
      <div
        class="m-auto flex w-80 max-w-lg -translate-y-8 flex-col gap-4 overflow-hidden rounded-lg bg-white px-12 pt-8"
        @click.stop=""
      >
        <div class="flex flex-col items-center gap-2">
          <div>
            <span
              class="material-symbols-outlined rounded-full border-4 text-8xl"
              :class="iconClass"
            >
              {{ props.variant === 'success' ? 'done' : 'exclamation' }}
            </span>
          </div>

          <h1 class="text-2xl font-semibold">
            {{ props.title }}
          </h1>

          <p class="text-center text-gray-500">
            {{ props.subtitle }}
          </p>
        </div>
        <div class="flex w-full items-center justify-center border-t border-gray-200 p-4">
          <div class="flex gap-2">
            <template v-if="props.type === 'close'">
              <button
                class="flex-1 rounded py-2 px-12 font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
                :class="buttonClass"
                @click="$emit('close')"
              >
                {{ props.buttonText }}
              </button>
            </template>
            <template v-if="props.type === 'confirm'">
              <button
                class="flex-1 rounded bg-gray-500 py-2 px-8 font-medium text-white hover:bg-gray-600 disabled:cursor-not-allowed disabled:opacity-60"
                @click="$emit('close')"
              >
                {{ props.buttonCancelText }}
              </button>
              <button
                class="flex-1 rounded py-2 px-8 font-medium text-white"
                :class="buttonClass"
                @click="$emit('confirm')"
              >
                {{ props.buttonConfirmText }}
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
 
<style scoped>
</style>