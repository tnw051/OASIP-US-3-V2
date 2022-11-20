<script setup lang="ts">
import { toFormValidator } from "@vee-validate/zod";
import { ErrorMessage, Field, useForm } from "vee-validate";
import { z } from "zod";

import { AxiosError } from "axios";
import { onBeforeMount, ref } from "vue";
import { Role } from "../gen-types";
import { createUser, getRoles } from "../service/api";

const roles = ref<Role[]>([]);

onBeforeMount(async () => {
  roles.value = await getRoles();
});

const validationSchema = toFormValidator(
  z.object({
    name: z.string().min(1, "Name is required").max(100, "Name exceeds 100 characters"),
    email: z.string().email("Email is invalid").max(50, "Email exceeds 50 characters"),
    password: z.string().min(8, "Password must be at least 8 characters"),
    confirmPassword: z.string(),
  }).refine((data) => data.password === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  }),
);

const { handleSubmit, setErrors, errors } = useForm<{
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: Role;
}>({
  validationSchema,
  initialValues: {
    name: "whatwhat",
    email: "what@what.com",
    password: "123456789",
    confirmPassword: "123456789",
    role: "ADMIN",
  },
});

const onSubmit = handleSubmit(async (user) => {
  try {
    const createdUser = await createUser({
      name: user.name,
      email: user.email,
      password: user.password,
      role: user.role,
    });

    if (createdUser) {
      alert("User created successfully");
    } else {
      alert("Failed to create user");
    }
  } catch (error) {
    if (error instanceof AxiosError) {
      if (error.response.status === 400) {
        setErrors(error.response.data.errors);
      }
    } else {
      alert("Something went wrong");
    }
  }
});
</script>
 
<template>
  <div class="flex h-full bg-gray-100">
    <form
      class="m-auto flex w-full max-w-sm flex-col gap-3 rounded-md bg-white p-6 pb-8 shadow"
      @submit.prevent="onSubmit"
    >
      <h1 class="mb-4 text-center text-xl font-medium">
        Create User
      </h1>
      <!-- Name -->
      <div class="flex flex-col gap-2">
        <label
          for="name"
          class="required text-sm font-medium text-gray-700"
        >Name</label>
        <Field
          id="name"
          name="name"
          class="rounded border bg-white p-1 px-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-sky-600/50" 
          placeholder="What's your name?"
        />
        <ErrorMessage
          name="name"
          class="-mt-1 rounded text-sm text-red-500"
        />
      </div>

      <!-- Email -->
      <div class="flex flex-col gap-2">
        <label
          for="email"
          class="required text-sm font-medium text-gray-700"
        >Email</label>
        <Field
          id="email"
          name="email"
          class="rounded border bg-white p-1 px-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-sky-600/50" 
          placeholder="What's your email?"
        />
        <ErrorMessage
          name="email"
          class="-mt-1 rounded text-sm text-red-500"
        />
      </div>

      <!-- Password -->
      <div class="flex flex-col gap-2">
        <label
          for="password"
          class="required text-sm font-medium text-gray-700"
        >Password</label>
        <Field
          id="password"
          name="password"
          type="password"
          class="rounded border bg-white p-1 px-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-sky-600/50" 
          placeholder="What's your password?"
        />
        <ErrorMessage
          name="password"
          class="-mt-1 rounded text-sm text-red-500"
        />
      </div>

      <!-- Confirm Password -->
      <div class="flex flex-col gap-2">
        <label
          for="confirmPassword"
          class="required text-sm font-medium text-gray-700"
        >Confirm password</label>
        <Field
          id="confirmPassword"
          name="confirmPassword"
          type="password"
          class="rounded border bg-white p-1 px-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-sky-600/50" 
          placeholder="Confirm your password"
        />
        <ErrorMessage
          name="confirmPassword"
          class="-mt-1 rounded text-sm text-red-500"
        />
      </div>

      <!-- Role -->
      <div class="flex flex-col gap-2">
        <label
          for="role"
          class="required text-sm font-medium text-gray-700"
        >Role</label>
        <Field
          id="category"
          name="role"
          as="select"
          class="rounded border bg-white p-1 px-2 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-sky-600/50" 
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
        </Field>

        <button
          type="submit"
          class="mt-4 rounded bg-sky-500 p-2 font-medium text-white hover:bg-sky-600 disabled:opacity-50"
          :disabled="Object.keys(errors).length > 0"
        >
          Create User
        </button>
      </div>
    </form>
  </div>
</template>
 
<style scoped>
.required::after {
  content: "*";
  @apply text-red-500 pl-1
}
</style>