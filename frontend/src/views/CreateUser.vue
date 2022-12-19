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

const { handleSubmit, setErrors, resetForm, values } = useForm<{
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  role: Role;
}>({
  validationSchema,
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

  resetForm();
});
</script>
 
<template>
  <div class="-mt-8 flex h-full">
    <form
      class="m-auto flex w-full max-w-sm flex-col gap-3 rounded-xl bg-white p-6 pb-8 shadow"
      @submit.prevent="onSubmit"
    >
      <h1 class="mb-1 text-xl font-medium">
        Create User
      </h1>

      <div class="mb-3 border-b border-gray-200" />

      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-1">
          <!-- Name -->
          <div class="flex flex-col gap-1">
            <label
              for="name"
              class="required text-sm font-medium text-slate-500"
            >Name</label>
            <Field
              id="name"
              name="name"
              class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
              placeholder="What's your name?"
            />
            <ErrorMessage
              name="name"
              class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
            />
          </div>

          <!-- Email -->
          <div class="flex flex-col gap-1">
            <label
              for="email"
              class="required text-sm font-medium text-slate-500"
            >Email</label>
            <Field
              id="email"
              name="email"
              class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
              placeholder="What's your email?"
            />
            <ErrorMessage
              name="email"
              class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
            />
          </div>
        </div>

        <div class="flex flex-col gap-1">
          <!-- Password -->
          <div class="flex flex-col gap-1">
            <label
              for="password"
              class="required text-sm font-medium text-slate-500"
            >Password</label>
            <Field
              id="password"
              name="password"
              type="password"
              class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
              placeholder="What's your password?"
            />
            <ErrorMessage
              name="password"
              class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
            />
          </div>

          <!-- Confirm Password -->
          <div class="flex flex-col gap-1">
            <label
              for="confirmPassword"
              class="required text-sm font-medium text-slate-500"
            >Confirm password</label>
            <Field
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
              placeholder="Confirm your password"
            />
            <ErrorMessage
              name="confirmPassword"
              class="-mt-1 rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
            />
          </div>
        </div>

        <!-- Role -->
        <div class="flex flex-col gap-1">
          <label
            for="role"
            class="required text-sm font-medium text-slate-500"
          >Role</label>
          <Field
            id="category"
            name="role"
            as="select"
            class="rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-1 focus:ring-sky-500"
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
        </div>

        <button
          type="submit"
          class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="Object.keys(values).some((key) => values[key] === undefined)"
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