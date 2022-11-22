<script setup>
import { computed, ref, watchEffect } from "vue";
import { useRoute } from "vue-router";
import { useOasipAuth } from "../auth/providers/oasip";
import { useAuthStore } from "../auth/useAuthStore";
import LoginMsal from "../components/LoginMsal.vue";
import router from "../router";

const { login } = useOasipAuth();
const { state: authState } = useAuthStore();
watchEffect(() => {
  if (authState.value.isAuthenticated) {
    router.push({ name: "home" });
  }
});

const route = useRoute();

function makeDefaultValues() {
  const defaultValue = "";
  return {
    email: defaultValue,
    password: defaultValue,
  };
}

const inputs = ref(makeDefaultValues());

const errors = ref({
  email: [],
  password: [],
});

const canSubmit = computed(() => {
  const noErrors = Object.values(errors.value).every(
    (error) => error.length === 0,
  );
  const noEmptyFields = Object.values(inputs.value).every(
    (value) => value !== "",
  );

  return noErrors && noEmptyFields;
});

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
  const emailRegex =
    /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
  if (!emailRegex.test(email)) {
    errors.value.email.push("Email is invalid");
  }
}

function validatePassword(e) {
  const password = e.target.value;
  errors.value.password = [];

  if (password.length > 14 || password.length < 8) {
    errors.value.password.push("Password must be between 8 and 14 characters");
  }

  if (password.length === 0) {
    errors.value.password.push("Password must not be blank");
  }
}

async function handleSubmit() {
  const user = {
    ...inputs.value,
  };

  const { success, error } = await login(user);
  if (success) {
    if (route.query.redirect) {
      console.log("[Login] redirecting to", route.query.redirect);
      await router.push(route.query.redirect);
    } else {
      console.log("[Login] redirecting to home");
      await router.push({ name: "home" });
    }
  } else {
    console.log("[Login] error", error);
    inputs.value.password = ""; // clear password every time user submits
    alert(error);
  }
}
</script>

<template>
  <div class="mt-8 w-full">
    <form
      class="m-auto flex max-w-sm flex-col gap-3 rounded-xl bg-white p-6 pb-8 shadow"
      @submit.prevent="handleSubmit"
    >
      <div class="mb-4 flex flex-col text-center text-gray-700">
        <h1 class="text-2xl font-medium">
          Login
        </h1>
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
          @input="validateEmail"
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
          @input="validatePassword"
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

      <button
        type="submit"
        class="mt-2 rounded bg-blue-500 py-2 px-4 font-medium text-white hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
        :disabled="!canSubmit"
      >
        Login
      </button>

      <div class="flex items-center justify-center">
        <div class="w-1/5 border-t border-gray-300" />
        <div class="mx-2 text-gray-500">
          or
        </div>
        <div class="w-1/5 border-t border-gray-300" />
      </div>

      <LoginMsal />
    </form>
  </div>
</template>

<style scoped>

</style>
