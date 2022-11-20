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
  <div class="flex h-screen flex-col bg-[#f7fafd]">
    <nav class="h-16 w-full shrink-0 grow-0 border-b bg-white px-8">
      <div class="mx-auto flex h-full items-center justify-between">
        <div class="flex items-center text-sm font-medium">
          <img
            src="https://cdn.7tv.app/emote/631210ee113e0e8575d2d130/4x.webp"
            width="64"
            class="mr-4"
          >
          <router-link
            to="/"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Events
          </router-link>
          <router-link
            v-if="!authState.isLecturer"
            :to="{ name: 'createEvent' }"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Create
            Event
          </router-link>
          <router-link
            :to="{ name: 'categories' }"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Categories
          </router-link>
          <router-link
            v-if="authState.isAdmin"
            :to="{ name: 'users' }"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Users
          </router-link>
          <router-link
            v-if="authState.isAdmin"
            :to="{ name: 'createUser' }"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Create User
          </router-link>
          <router-link
            v-if="!authState.isAuthenticated"
            :to="{ name: 'login' }"
            class="rounded-md px-4 py-3 text-gray-500 transition hover:bg-gray-100 hover:text-gray-700"
          >
            Login
          </router-link>
        </div>
        <div class="flex items-center text-sm text-gray-700">
          <div
            v-if="authState.user"
            class="flex flex-col text-xs font-medium"
          >
            <span
              v-if="authState.isAuthenticated"
              class="pr-4 font-medium"
            >{{ authState.user.name }}</span>
            <span
              v-if="authState.isGuest"
              class="text-gray-500"
            >Guest</span>
            <span
              v-else-if="authState.isAuthenticated"
              class="text-sky-500"
            >{{ authState.user.role }}</span>
          </div>
          <button
            v-if="authState.isAuthenticated"
            class="ml-4 cursor-pointer rounded-md border border-gray-200 p-2 text-gray-700 hover:text-sky-500"
            @click="handleLogout"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>

    <router-view class="grow" />
  </div>
</template>

<style scoped>
.router-link-active {
  @apply text-sky-600 bg-sky-50;
  /* @apply border-blue-500 border-b-2 text-blue-500; */
}
</style>