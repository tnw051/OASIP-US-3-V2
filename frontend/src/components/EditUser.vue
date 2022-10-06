<script setup>
import { computed, ref } from "vue";

const props = defineProps({
  currentUser: {
    type: Object,
    default: () => ({}),
  },
  roles: {
    type: Array,
    default: () => [],
  },
});

const emits = defineEmits([
  "save",
  "cancel",
]);


function makeDefaultValues() {
  const defaultValue = "";
  return {
    name: defaultValue,
    email: defaultValue,
    role: "STUDENT",
  };
}

const inputs = ref(makeDefaultValues());

const errors = ref({
  name: [],
  email: [],
});

const canSubmit = computed(() => {
  const noErrors = Object.values(errors.value).every((error) => error.length === 0);
  const noEmptyFields = Object.values(inputs.value).every((value) => value !== "");

  return noErrors && noEmptyFields;
});

function validateName(e) {
  const name = e.target.value;
  errors.value.name = [];

  if (name.length > 100) {
    errors.value.name.push("Name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.value.name.push("Name must not be blank");
  }
}

function validateEmail(e) {
  const email = e.target.value;
  errors.value.email = [];

  if (email.length > 50) {
    errors.value.email.push("Email must be less than 50 characters");
  }

  if (email.trim().length === 0) {
    errors.value.email.push("Email must not be blank");
  }

  // RFC2822 https://regexr.com/2rhq7
  const emailRegex = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
  if (!emailRegex.test(email)) {
    errors.value.email.push("Email is invalid");
  }
}

inputs.value = {
  name: props.currentUser.name,
  email: props.currentUser.email,
  role: props.currentUser.role,
};

function handleSaveClick() {
  const updates = {};

  if (inputs.value.name.trim() !== props.currentUser.name) {
    updates.name = inputs.value.name;
  }

  if (inputs.value.email.trim() !== props.currentUser.email) {
    updates.email = inputs.value.email;
  }

  if (inputs.value.role !== props.currentUser.role) {
    updates.role = inputs.value.role;
  }

  if (Object.keys(updates).length == 0) {
    emits("cancel");
  } else {
    emits("save", updates);
  }
}

// computed for each fields whether it is changed
const isNameChanged = computed(() => inputs.value.name.trim() !== props.currentUser.name);
const isEmailChanged = computed(() => inputs.value.email.trim() !== props.currentUser.email);
const isRoleChanged = computed(() => inputs.value.role !== props.currentUser.role);

const isChanged = computed(() => isNameChanged.value || isEmailChanged.value || isRoleChanged.value);
</script>
 
<template>
  <div
    class=" flex w-full flex-col gap-3 break-words rounded-2xl border-b-2 border-white/50 bg-white p-6 shadow-xl shadow-black/5"
  >
    <div class="flex flex-col gap-2">
      <label
        for="name"
        class="text-sm font-medium text-gray-700"
        :class="{ 'font-bold changed': isNameChanged }"
      >Name</label>
      <input
        id="name"
        v-model="inputs.name"
        type="text"
        class="rounded bg-gray-100 p-2"
        placeholder="What's your name?"
        @input="validateName"
      >
      <div
        v-if="errors.name.length > 0"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.name"
          :key="error"
        >{{ error }}</span>
      </div>
    </div>

    <div class="flex flex-col gap-2">
      <label
        for="email"
        class="text-sm font-medium text-gray-700"
        :class="{ 'font-bold changed': isEmailChanged }"
      >Email</label>
      <input
        id="email"
        v-model="inputs.email"
        type="email"
        class="rounded bg-gray-100 p-2"
        placeholder="What's your email?"
        @input="validateEmail"
      >
      <div
        v-if="errors.email.length > 0"
        class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
      >
        <span
          v-for="error in errors.email"
          :key="error"
        >{{ error }}</span>
      </div>
    </div>

    <div class="flex flex-col gap-2">
      <label
        for="role"
        class="text-sm font-medium text-gray-700"
        :class="{ 'font-bold changed': isRoleChanged }"
      >Role</label>
      <select
        id="category"
        v-model="inputs.role"
        class="rounded bg-gray-100 p-2"
      >
        <option
          disabled
          selected
          value=""
        >
          Select role
        </option>
        <option
          v-for="role in roles"
          :key="role"
          :value="role"
        >
          {{ role }}
        </option>
      </select>
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
        :disabled="!canSubmit || !isChanged"
        @click="handleSaveClick"
      >
        Save
      </button>
    </div>
  </div>
</template>
 
<style scoped>
.changed::after {
  content: '*';
  @apply pl-1
}
</style>