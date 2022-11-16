<script setup lang="ts">
import { useMsalAuth } from "../auth/providers/msal";

const { login } = useMsalAuth();

</script>
 
<template>
  <button
    class="rounded-md border-2 border-gray-300 p-2 font-medium text-gray-600 hover:text-sky-600"
    type="button"
    @click="login"
  >
    Sign in with Microsoft
  </button>
</template>
 
<style scoped>

</style>