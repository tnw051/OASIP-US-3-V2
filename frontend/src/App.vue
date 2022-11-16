<script setup lang="ts">
import { watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "./auth/useAuthStore";

const router = useRouter();

const { state: authState, logout } = useAuthStore();
console.log(authState);

watch(authState, () => {
  console.log("App: authState changed");
});

async function handleLogout() {
  const success = await logout();
  if (success) {
    router.push({ name: "login" });
  } else {
    alert("Something went wrong");
  }
}
</script>
 
<template>
  <nav class="flex items-center justify-between border-b border-gray-200 bg-white px-12 py-4">
    <div class="flex items-center gap-1 text-sm font-medium">
      <img
        src="https://cdn.7tv.app/emote/611cb0c5f20f644c3fadb992/3x"
        width="64"
      >
      <img
        src="https://cdn.betterttv.net/emote/60a21baf67644f1d67e87a6c/3x"
        width="64"
      >
      <router-link
        to="/"
        class="ml-6 rounded-md p-2 text-gray-700 hover:text-sky-600"
      >
        Events
      </router-link>
      <router-link
        v-if="!authState.isLecturer"
        :to="{ name: 'createEvent' }"
        class="rounded-md p-2 text-gray-700 hover:text-sky-600"
      >
        Create
        Event
      </router-link>
      <router-link
        :to="{ name: 'categories' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Categories
      </router-link>
      <router-link
        v-if="authState.isAdmin"
        :to="{ name: 'users' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Users
      </router-link>
      <router-link
        v-if="authState.isAdmin"
        :to="{ name: 'createUser' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Create User
      </router-link>
      <router-link
        v-if="!authState.isAuthenticated"
        :to="{ name: 'login' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Login
      </router-link>
    </div>
    <div class="flex items-center text-sm text-gray-700">
      <div class="flex flex-col text-xs font-medium">
        <span v-if="authState.isAuthenticated">{{ authState.user?.name }}</span>
        <span
          v-if="authState.isGuest"
          class="text-gray-500"
        >Guest</span>
        <span
          v-else-if="authState.isAuthenticated"
          class="text-blue-400"
        >{{ authState.user.role }}</span>
      </div>
      <a
        v-if="authState.isAuthenticated"
        class="ml-4 cursor-pointer rounded-md border border-gray-200 p-2 text-gray-700 hover:text-sky-600"
        @click="handleLogout"
      >Logout</a>
    </div>
  </nav>

  <router-view />
</template>
 
<style scoped>
.router-link-active {
  @apply text-sky-600 bg-sky-50;
}
</style>