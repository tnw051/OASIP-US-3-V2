<script setup lang="ts">
import { computed, ref } from "vue";
import { CategoryResponse } from "../gen-types";
import { validateDescription, validateDuration, validateName } from "../utils/validators/category";

interface Props {
  category: CategoryResponse;
  categories: CategoryResponse[];
}

const props = defineProps<Props>();

const emits = defineEmits([
  "save",
  "cancel",
]);

const errors = ref<{
  name: string[];
  description: string[];
  duration: string[];
}>({
  name: [],
  description: [],
  duration: [],
});

const inputs = ref({
  name: props.category.eventCategoryName,
  description: props.category.eventCategoryDescription || "",
  duration: props.category.eventDuration,
});

function handleNameInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.name = validateName(target.value, props.categories).errors;
}

function handleDurationInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.duration = validateDuration(Number(target.value)).errors;
}

function handleDescriptionInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.description = validateDescription(target.value).errors;
}

function handleSaveClick() {
  emits("save", {
    eventCategoryName: inputs.value.name,
    eventCategoryDescription: inputs.value.description,
    eventDuration: inputs.value.duration,
  });
}

const canSubmit = computed(() => {
  const noErrors = Object.values(errors.value).every((error) => error.length === 0);

  return noErrors;
});
</script>
 
<template>
  <div
    class=" flex w-full flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5"
  >
    <div class="flex flex-col gap-2">
      <label
        for="name"
        class="required text-sm font-medium text-gray-700"
      >Category Name</label>
      <input
        id="name"
        v-model="inputs.name"
        type="text"
        required
        class="rounded bg-gray-100 p-2"
        @input="handleNameInput"
      >
      <div
        v-if="errors.name.length > 0"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.name"
          :key="error"
        >
          {{ error }}
        </span>
      </div>
    </div>

    <div class="flex flex-col gap-2">
      <label
        for="duration"
        class="required text-sm font-medium text-gray-700"
      >Duration</label>
      <input
        id="duration"
        v-model="inputs.duration"
        type="number"
        required
        class="rounded bg-gray-100 p-2"
        min="1"
        max="480"
        @input="handleDurationInput"
      >
      <div
        v-if="errors.duration.length > 0"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.duration"
          :key="error"
        >
          {{ error }}
        </span>
      </div>
    </div>

    <div class="flex flex-col gap-2">
      <label
        for="descriptions"
        class="text-sm font-medium text-gray-700"
      >Description <span
        class="font-normal text-gray-400"
      >(optional)</span>
      </label>
      <textarea
        id="descriptions"
        v-model="inputs.description"
        class="rounded bg-gray-100 p-2"
        placeholder="What's your category about?"
        @input="handleDescriptionInput"
      />
      <div
        v-if="errors.description.length > 0"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.description"
          :key="error"
        >
          {{ error }}
        </span>
      </div>
    </div>


    <div class="flex gap-2">
      <button
        class="mt-2 flex-1 rounded bg-gray-500 py-2 px-4 font-medium text-white hover:bg-gray-600"
        @click="$emit('cancel')"
      >
        Cancel
      </button>

      <button
        type="submit"
        class="mt-2 flex-1 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="!canSubmit"
        @click="handleSaveClick"
      >
        Save
      </button>
    </div>
  </div>
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>