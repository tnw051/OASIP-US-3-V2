<script setup>
</script>
 
<template>
  <nav class="bg-white px-12 py-4 flex border-b border-gray-200">
    <div class="flex items-center gap-1 text-sm font-medium">
      <router-link to="/" class="p-2 rounded-md text-gray-700 hover:text-sky-600">Events</router-link>
      <router-link :to="{ name: 'createEvent' }" class="p-2 rounded-md text-gray-700 hover:text-sky-600">Create
        Event</router-link>
      <router-link :to="{ name: 'categories' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Categories</router-link>
      <router-link :to="{ name: 'users' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Users</router-link>
      <router-link :to="{ name: 'createUser' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Create User</router-link>
      <router-link :to="{ name: 'match' }" class="p-2 ml-2 text-gray-700 hover:text-sky-600">Login</router-link>
    </div>
  </nav>

  <router-view></router-view>
</template>
 
<style scoped>
.router-link-active {
  @apply text-sky-600 bg-sky-50;
}
</style>