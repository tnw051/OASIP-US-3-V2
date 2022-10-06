<script setup>
import { useRouter } from "vue-router";
import { useAuth } from "./utils/useAuth";

const router = useRouter();
const { logout, isAdmin, isLecturer, isAuthenticated, user } = useAuth();

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
        v-if="!isLecturer"
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
        v-if="isAdmin"
        :to="{ name: 'users' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Users
      </router-link>
      <router-link
        v-if="isAdmin"
        :to="{ name: 'createUser' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Create User
      </router-link>
      <router-link
        v-if="!isAuthenticated"
        :to="{ name: 'login' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Login
      </router-link>
    </div>
    <div class="text-sm text-gray-700">
      <span
        v-if="isAuthenticated"
        class="font-medium"
      >{{ user.sub }}</span>
      <a
        v-if="isAuthenticated"
        class="ml-2 cursor-pointer p-2 text-gray-700 hover:text-sky-600"
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