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
      class="absolute inset-0 bg-black/50 flex text-slate-700 shadow-2xl z-50"
      @click="$emit('close')"
    >
      <div
        class="m-auto -translate-y-8 pt-8 px-12 bg-white flex flex-col gap-4 max-w-lg w-80 rounded-lg overflow-hidden"
        @click.stop=""
      >
        <div class="flex flex-col items-center gap-2">
          <div>
            <span
              class="material-symbols-outlined text-8xl border-4 rounded-full"
              :class="iconClass"
            >
              {{ props.variant === 'success' ? 'done' : 'exclamation' }}
            </span>
          </div>

          <h1 class="font-semibold text-2xl">
            {{ props.title }}
          </h1>

          <p class="text-gray-500 text-center">
            {{ props.subtitle }}
          </p>
        </div>
        <div class="flex items-center justify-center p-4 border-t border-gray-200 w-full">
          <div class="flex gap-2">
            <template v-if="props.type === 'close'">
              <button
                class="text-white font-medium py-2 px-12 rounded disabled:opacity-60 disabled:cursor-not-allowed flex-1"
                :class="buttonClass"
                @click="$emit('close')"
              >
                {{ props.buttonText }}
              </button>
            </template>
            <template v-if="props.type === 'confirm'">
              <button
                class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-8 rounded disabled:opacity-60 disabled:cursor-not-allowed flex-1"
                @click="$emit('close')"
              >
                {{ props.buttonCancelText }}
              </button>
              <button
                class="text-white font-medium py-2 px-8 rounded flex-1"
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