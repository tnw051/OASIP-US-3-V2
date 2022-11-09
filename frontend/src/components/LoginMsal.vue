<script setup lang="ts">
import { useMsal } from "../composables/useMsal";
import { loginRequest } from "../configs/msalAuthConfig";

const { instance } = useMsal();

const loginRedirect = () => {
  instance.loginRedirect(loginRequest);
};
</script>
 
<template>
  <button
    class="rounded-md border-2 border-gray-300 p-2 font-medium text-gray-600 hover:text-sky-600"
    type="button"
    @click="loginRedirect"
  >
    Sign in with Microsoft
  </button>
</template>
 
<style scoped>

</style>