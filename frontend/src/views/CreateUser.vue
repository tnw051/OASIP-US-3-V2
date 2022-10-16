<script setup lang="ts">
import { onBeforeMount, ref } from "vue";
import Modal from "../components/Modal.vue";
import { Role } from "../gen-types";
import router from "../router";
import { createUser, getRoles } from "../service/api";
import { ApiErrorError } from "../service/common";
import { useUserValidator } from "../utils/useUserValidator";

const roles = ref<Role[]>([]);
const { inputs, errors, canSubmit } = useUserValidator();

onBeforeMount(async () => {
  roles.value = await getRoles();
});

const isSuccessModalOpen = ref(false);
const isErrorModalOpen = ref(false);

async function handleSubmit() {
  const user = {
    ...inputs,
  };

  try {
    const createdUser = await createUser({
      name: user.name,
      email: user.email,
      password: user.password,
      role: user.role,
    });

    if (createdUser) {
      isSuccessModalOpen.value = true;
    } else {
      isErrorModalOpen.value = true;
    }
  } catch (error) {
    if (error instanceof ApiErrorError) {
      if (error.content.status === 400) {
        Object.assign(errors, error.content.errors);
        return;
      }
    }

    isErrorModalOpen.value = true;
  }
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
        >
        <div
          v-if="errors.name"
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
        >
        <div
          v-if="errors.email"
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
        >
        <div
          v-if="errors.password"
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
        >
        <div
          v-if="errors.confirmPassword"
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
    @close="router.go(0)"
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