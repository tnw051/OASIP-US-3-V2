<script setup lang="ts">
import { computed, onBeforeMount, ref } from "vue";
import Modal from "../components/Modal.vue";
import { Role } from "../gen-types";
import { createUser, getRoles } from "../service/api";
import { validateName, validateEmail, validatePassword, validateConfirmPassword } from "../utils/validators/user";

const roles = ref<Role[]>([]);

onBeforeMount(async () => {
  roles.value = await getRoles();
});

function makeDefaultValues() {
  const defaultValue = "";
  return {
    name: defaultValue,
    email: defaultValue,
    password: defaultValue,
    confirmPassword: defaultValue,
    role: "STUDENT",
  };
}

const inputs = ref(makeDefaultValues());

const errors = ref({
  name: [],
  email: [],
  password: [],
  confirmPassword: [],
});

const canSubmit = computed(() => {
  const noErrors = Object.values(errors.value).every((error) => error.length === 0);
  const noEmptyFields = Object.values(inputs.value).every((value) => value !== "");

  return noErrors && noEmptyFields;
});

function handleNameInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.name = validateName(target.value).errors;
}

function handleEmailInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.email = validateEmail(target.value).errors;
}

function handlePasswordInput(e: Event) {
  const target = e.target as HTMLInputElement;
  errors.value.password = validatePassword(target.value).errors;
}

function handleConfirmPasswordInput(e: Event) {
  const target = e.target as HTMLInputElement;
  const password = inputs.value.password;
  errors.value.confirmPassword = validateConfirmPassword(password, target.value).errors;
}

const isSuccessModalOpen = ref(false);
const isErrorModalOpen = ref(false);

async function handleSubmit() {
  const user = {
    ...inputs.value,
  };
  delete user.confirmPassword;

  try {
    const createdUser = await createUser(user);

    if (createdUser) {
      resetInputs();
      isSuccessModalOpen.value = true;
    } else {
      isErrorModalOpen.value = true;
    }
  } catch (errorResponse) {
    if (errorResponse.status !== 400) {
      isErrorModalOpen.value = true;
      return;
    }

    Object.assign(errors.value, errorResponse.errors);
  }
}

function resetInputs() {
  inputs.value = makeDefaultValues();
}
</script>
 
<template>
  <div class="mx-auto mt-8 max-w-md">
    <form
      class="flex flex-col gap-4 rounded-xl border border-gray-100 bg-white py-10 px-8 shadow-xl shadow-black/5"
      @submit.prevent="handleSubmit"
    >
      <div class="mb-4 flex flex-col text-center text-gray-700">
        <h1 class="text-2xl font-medium">
          Create User
        </h1>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="name"
          class="required text-sm font-medium text-gray-700"
        >Name</label>
        <input
          id="name"
          v-model="inputs.name"
          type="text"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your name?"
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
          for="email"
          class="required text-sm font-medium text-gray-700"
        >Email</label>
        <input
          id="email"
          v-model="inputs.email"
          type="email"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your email?"
          @input="handleEmailInput"
        >
        <div
          v-if="errors.email.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.email"
            :key="error"
          >
            {{ error }}
          </span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="password"
          class="required text-sm font-medium text-gray-700"
        >Password</label>
        <input
          id="password"
          v-model="inputs.password"
          type="password"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="What's your password?"
          @input="handlePasswordInput"
        >
        <div
          v-if="errors.password.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.password"
            :key="error"
          >
            {{ error }}
          </span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="confirmPassword"
          class="required text-sm font-medium text-gray-700"
        >Confirm password</label>
        <input
          id="confirmPassword"
          v-model="inputs.confirmPassword"
          type="password"
          required
          class="rounded bg-gray-100 p-2"
          placeholder="Confirm your password"
          @input="handleConfirmPasswordInput"
        >
        <div
          v-if="errors.confirmPassword.length > 0"
          class="mx-1 flex flex-col rounded-md bg-red-50 py-1 px-2 text-sm text-red-500"
        >
          <span
            v-for="error in errors.confirmPassword"
            :key="error"
          >
            {{ error }}
          </span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label
          for="role"
          class="required text-sm font-medium text-gray-700"
        >Role</label>
        <select
          id="category"
          v-model="inputs.role"
          required
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

      <button
        type="submit"
        class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="!canSubmit"
      >
        Create User
      </button>
    </form>
  </div>

  <Modal
    title="Success"
    subtitle="User created successfully"
    :is-open="isSuccessModalOpen"
    @close="isSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isErrorModalOpen"
    variant="error"
    @close="isErrorModalOpen = false"
  />
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>