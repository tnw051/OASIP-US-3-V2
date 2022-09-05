<script setup>
import { computed, ref } from "vue";
import { login } from "../service/api";

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
    (error) => error.length === 0
  );
  const noEmptyFields = Object.values(inputs.value).every(
    (value) => value !== ""
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

  try {
    login(user, {
      onSuccess: (response) => {
        console.log(response);
        alert("Login successful");
      },
      onUnauthorized: (error) => {
        console.log(error);
        alert("Password is incorrect");
      },
      onNotFound: (error) => {
        console.log(error);
        alert("A user with the specified email DOES NOT exist");
      },
    });
  } catch (errorResponse) {
    alert(errorResponse.message);
  }

  inputs.value.password = ""; // clear password every time user submits
}
</script>

<template>
  <div class="max-w-md mx-auto mt-8">
    <form
      @submit.prevent="handleSubmit"
      class="flex flex-col gap-4 bg-white py-10 px-8 border border-gray-100 rounded-xl shadow-xl shadow-black/5"
    >
      <div class="flex flex-col text-center mb-4 text-gray-700">
        <h1 class="font-medium text-2xl">Login</h1>
      </div>

      <div class="flex flex-col gap-2">
        <label for="email" class="required text-sm font-medium text-gray-700"
          >Email</label
        >
        <input
          id="email"
          type="email"
          v-model="inputs.email"
          required
          class="bg-gray-100 p-2 rounded"
          @input="validateEmail"
        />
        <div
          v-if="errors.email.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col"
        >
          <span v-for="error in errors.email">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="password" class="required text-sm font-medium text-gray-700"
          >Password</label
        >
        <input
          id="password"
          type="password"
          v-model="inputs.password"
          required
          class="bg-gray-100 p-2 rounded"
          @input="validatePassword"
        />
        <div
          v-if="errors.password.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col"
        >
          <span v-for="error in errors.password">{{ error }}</span>
        </div>
      </div>

      <button
        type="submit"
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded disabled:opacity-60 disabled:cursor-not-allowed mt-2"
        :disabled="!canSubmit"
      >
        Match Password
      </button>
    </form>
  </div>
</template>

<style scoped></style>
