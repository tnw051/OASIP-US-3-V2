<script setup>
import { useRouter } from "vue-router";
import { useAuth } from "./utils/useAuth";

const router = useRouter();
const { logout, isAdmin, isLecturer, isAuthenticated, user } = useAuth();

async function handleLogout() {
  await logout();
  await router.go(0);
}
</script>

<template>
  <div class="flex h-screen flex-col bg-zinc-50">
    <nav class="w-full border-b bg-white px-8">
      <div class="mx-auto flex w-full items-center justify-between">
        <div class="flex items-center gap-1 text-sm font-medium">
          <img
            src="https://cdn.7tv.app/emote/631210ee113e0e8575d2d130/4x.webp"
            width="64"
            class="mr-4"
          >
          <router-link
            to="/"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Events
          </router-link>
          <router-link
            v-if="!isLecturer"
            :to="{ name: 'createEvent' }"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Create
            Event
          </router-link>
          <router-link
            :to="{ name: 'categories' }"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Categories
          </router-link>
          <router-link
            v-if="isAdmin"
            :to="{ name: 'users' }"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Users
          </router-link>
          <router-link
            v-if="isAdmin"
            :to="{ name: 'createUser' }"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Create User
          </router-link>
          <router-link
            v-if="!isAuthenticated"
            :to="{ name: 'login' }"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Login
          </router-link>
        </div>
        <div class="text-sm text-gray-700">
          <span
            v-if="isAuthenticated"
            class="pr-4 font-medium"
          >{{ user.sub }}</span>
          <button
            v-if="isAuthenticated"
            class="p-2 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
            @click="handleLogout"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>

    <router-view />
  </div>
</template>

<style scoped>
.router-link-active {
  /* @apply text-sky-600 bg-sky-50; */
  @apply border-blue-600 border-b-2 text-blue-600;
}
</style>