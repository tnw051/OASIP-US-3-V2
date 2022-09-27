<script setup>
import { useRouter } from 'vue-router';
import { useAuth } from './utils/useAuth';

const router = useRouter()
const { logout } = useAuth();

async function handleLogout() {
  const success = await logout();
  if (success) {
    router.push({ name: 'login' })
  } else {
    alert('Something went wrong')
  }
}
</script>
 
<template>
  <nav class="bg-white px-12 py-4 flex border-b border-gray-200">
    <div class="flex items-center gap-1 text-sm font-medium">
      <router-link to="/" class="p-2 rounded-md text-gray-700 hover:text-sky-600">Events</router-link>
      <router-link :to="{ name: 'createEvent' }" class="p-2 rounded-md text-gray-700 hover:text-sky-600">Create
        Event</router-link>
      <router-link :to="{ name: 'categories' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Categories
      </router-link>
      <router-link :to="{ name: 'users' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Users</router-link>
      <router-link :to="{ name: 'createUser' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Create User
      </router-link>
      <router-link :to="{ name: 'login' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Login</router-link>
      <a @click="handleLogout" class="cursor-pointer p-2 ml-2 text-gray-700 hover:text-sky-600">Logout</a>
    </div>
  </nav>

  <router-view></router-view>
</template>
 
<style scoped>
.router-link-active {
  @apply text-sky-600 bg-sky-50;
}
</style>