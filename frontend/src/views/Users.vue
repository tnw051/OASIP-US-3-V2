<script setup>
import { getUsers } from "../service/api";
import { ref, onBeforeMount } from "vue";

const users = ref([]);

onBeforeMount(async () => {
  users.value = await getUsers();
});
</script>

<template>
  <div class="py-8 px-12 max-w-[1440px] flex mx-auto">
    <div class="flex flex-col text-slate-700">
      <h1 class="font-semibold text-4xl">Users</h1>
      <div class="flex justify-between mb-4">
        <div class="mb-4 mt-2">{{ users.length }} users shown</div>
      </div>
      <div class="flex">
        <table
          class="table-fixed text-left w-8/12 flex-1 break-words border border-slate-200 shadow-xl shadow-black/5 p-4 h-full"
        >
          <thead
            class="text-xs text-slate-500 uppercase bg-slate-100 text-left"
          >
            <tr>
              <th class="pl-2 py-3">Name</th>
              <th class="pl-2 py-3">Email</th>
              <th class="pl-2 py-3">Role</th>
            </tr>
          </thead>

          <tr
            v-if="users.length > 0"
            v-for="user in users"
            class="my-10 bg-white rounded-lg border-b border-gray-200 shadow-black/5 relative hover:bg-gray-50 transition box-border"
            :class="[
              {
                'z-10 bg-blue-200/10 hover:bg-blue-200/20 ring-2 ring-blue-400/50 ': false,
              },
            ]"
          >
            <td class="py-2 px-2">
              <span class="font-medium">{{ user.name }}</span>
            </td>

            <td class="py-2 px-2">
              <span class="font-medium">{{ user.email }}</span>
            </td>

            <td class="py-2 px-2">
              <span class="font-medium">{{ user.role }}</span>
            </td>

            <!-- <td class="py-2 px-2">
              <div class="flex">
                <button
                  @click.stop="startEdit(category)"
                  class="text-slate-400 hover:text-yellow-500 disabled:hover:text-slate-400 text-xs flex items-center justify-center w-8 h-8 rounded-full transition"
                  :disabled="isEditing"
                >
                  <span class="material-symbols-outlined"> edit </span>
                </button>
              </div>
            </td> -->
          </tr>
        </table>

        <!-- <div class="p-4 bg-slate-100 relative w-4/12" v-if="currentCategory.id">
          <EditCategory
            class="sticky top-24"
            :category="currentCategory"
            :categories="categories"
            @cancel="stopEdit"
            v-if="isEditing"
            @save="saveCategory"
          />
        </div> -->
      </div>
    </div>
  </div>
</template>

<style scoped></style>
