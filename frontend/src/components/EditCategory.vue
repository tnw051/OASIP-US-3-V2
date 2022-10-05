<script setup>
import { computed, ref } from "vue";

const props = defineProps({
  category: {
    type: Object,
    default: () => ({}),
  },
  categories: {
    type: Array,
    default: () => [],
  },
});

const emits = defineEmits([
  "save",
  "cancel",
]);

const errors = ref({
  name: [],
  description: [],
  duration: [],
});

const inputs = ref({
  name: props.category.eventCategoryName,
  description: props.category.eventCategoryDescription || "",
  duration: props.category.eventDuration,
});

function validateName(e) {
  const name = e.target.value;
  errors.value.name = [];

  if (name.length > 100) {
    errors.value.name.push("Category name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.value.name.push("Category name must not be blank");
  }

  if (!isNameUnique(name)) {
    errors.value.name.push("Category name is not unique");
  }
}

function isNameUnique(name) {
  const existingCategory = props.categories.find((category) => category.eventCategoryName.toLowerCase() === name.trim().toLowerCase() && category.id != props.category.id);
  if (existingCategory) {
    return false;
  }

  return true;
}

function validateDuration(e) {
  const duration = e.target.value;
  errors.value.duration = [];

  if (duration < 1 || duration > 480) {
    errors.value.duration.push("Category duration must be between 1 and 480 minutes");
  }
}

function validateDescription(e) {
  const description = e.target.value;
  errors.value.description = [];

  if (description.length > 500) {
    errors.value.description.push("Category descriptions must be less than 500 characters");
  }
}

function handleSaveClick() {
  emits("save", {
    eventCategoryName: inputs.value.name,
    eventCategoryDescription: inputs.value.description,
    eventDuration: inputs.value.duration,
  });
}

const canSubmit = computed(() => {
  const noErrors = Object.values(errors.value).every((error) => error === false || error.length === 0);

  return noErrors;
});
</script>
 
<template>
  <div
    class=" bg-white p-6 rounded-2xl flex flex-col gap-3 shadow-xl border-b-2 border-white/50 shadow-black/5 break-words w-full"
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
        class="bg-gray-100 p-2 rounded"
        @input="validateName"
      >
      <div
        v-if="errors.name.length > 0"
        class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col"
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
        class="bg-gray-100 p-2 rounded"
        min="1"
        max="480"
        @input="validateDuration"
      >
      <div
        v-if="errors.duration.length > 0"
        class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col"
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
        class="text-gray-400 font-normal"
      >(optional)</span>
      </label>
      <textarea
        id="descriptions"
        v-model="inputs.description"
        class="bg-gray-100 p-2 rounded"
        placeholder="What's your category about?"
        @input="validateDescription"
      />
      <div
        v-if="errors.description.length > 0"
        class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col"
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
        class="bg-gray-500 hover:bg-gray-600 text-white font-medium py-2 px-4 rounded mt-2 flex-1"
        @click="$emit('cancel')"
      >
        Cancel
      </button>

      <button
        type="submit"
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded disabled:opacity-60 disabled:cursor-not-allowed mt-2 flex-1"
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