<script setup>
import { computed, onBeforeMount, ref } from 'vue';
import Modal from '../components/Modal.vue';
import { createUser, getRoles } from '../service/api';

const roles = ref([]);

onBeforeMount(async () => {
  roles.value = await getRoles();
});

function makeDefaultValues() {
  const defaultValue = '';
  return {
    name: defaultValue,
    email: defaultValue,
    password: defaultValue,
    confirmPassword: defaultValue,
    role: "STUDENT"
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
  const noEmptyFields = Object.values(inputs.value).every((value) => value !== '');

  return noErrors && noEmptyFields;
});

function validateName(e) {
  const bookingName = e.target.value;
  errors.value.name = [];

  if (bookingName.length > 100) {
    errors.value.name.push("Name must be less than 100 characters");
  }

  if (bookingName.trim().length === 0) {
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

function validatePassword(e) {
  const password = e.target.value;
  errors.value.password = [];
  
  if (password.length > 14 || password.length < 8) {
    errors.value.password.push("Password must be between 8 and 14 characters")
  }

  if (password.length === 0) {
    errors.value.password.push("Password must not be blank");
  }
}

function validateConfirmPassword(e) {
  const password = inputs.value.password;
  const confirmPassword = e.target.value;
  errors.value.confirmPassword = [];

  if (confirmPassword !== password) {
    errors.value.confirmPassword.push("Passwords do not match");
  }
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
  <div class="max-w-md mx-auto mt-8">
    <form @submit.prevent="handleSubmit"
      class="flex flex-col gap-4 bg-white py-10 px-8 border border-gray-100 rounded-xl shadow-xl shadow-black/5">

      <div class="flex flex-col text-center mb-4 text-gray-700">
        <h1 class="font-medium text-2xl">Create User</h1>
      </div>

      <div class="flex flex-col gap-2">
        <label for="name" class="required text-sm font-medium text-gray-700">Name</label>
        <input id="name" type="text" v-model="inputs.name" required class="bg-gray-100 p-2 rounded"
          @input="validateName" placeholder="What's your name?">
        <div v-if="errors.name.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.name">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="email" class="required text-sm font-medium text-gray-700">Email</label>
        <input id="email" type="email" v-model="inputs.email" required class="bg-gray-100 p-2 rounded"
          @input="validateEmail" placeholder="What's your email?">
        <div v-if="errors.email.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.email">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="password" class="required text-sm font-medium text-gray-700">Password</label>
        <input id="password" type="password" v-model="inputs.password" required class="bg-gray-100 p-2 rounded"
          @input="validatePassword" placeholder="What's your password?">
        <div v-if="errors.password.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.password">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="confirmPassword" class="required text-sm font-medium text-gray-700">Confirm password</label>
        <input id="confirmPassword" type="password" v-model="inputs.confirmPassword" required class="bg-gray-100 p-2 rounded"
          @input="validateConfirmPassword" placeholder="Confirm your password">
        <div v-if="errors.confirmPassword.length > 0"
          class="text-red-500 text-sm bg-red-50 py-1 px-2 mx-1 rounded-md flex flex-col">
          <span v-for="error in errors.confirmPassword">{{ error }}</span>
        </div>
      </div>

      <div class="flex flex-col gap-2">
        <label for="role" class="required text-sm font-medium text-gray-700">Role</label>
        <select v-model="inputs.role" required class="bg-gray-100 p-2 rounded" id="category">
          <option disabled selected value="">Select role</option>
          <option v-for="role in roles" :value="role">{{ role }}</option>
        </select>
      </div>

      <button type="submit"
        class="bg-blue-500 hover:bg-blue-600 text-white font-medium py-2 px-4 rounded disabled:opacity-60 disabled:cursor-not-allowed mt-2"
        :disabled="!canSubmit">Create User</button>
    </form>
  </div>

  <Modal title="Success" subtitle="User created successfully" :is-open="isSuccessModalOpen"
    @close="isSuccessModalOpen = false" />

  <Modal title="Error" subtitle="Something went wrong" button-text="Try Again" :is-open="isErrorModalOpen"
    variant="error" @close="isErrorModalOpen = false" />
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>