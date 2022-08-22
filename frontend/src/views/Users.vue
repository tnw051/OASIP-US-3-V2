<script setup>
import { computed } from "@vue/reactivity";
import { onBeforeMount, ref } from "vue";
import Table from "../components/Table.vue";
import { getUsers } from "../service/api";
import { formatDateTime } from "../utils";

const users = ref([]);
const showDetails = ref(false);

onBeforeMount(async () => {
  users.value = await getUsers();
});

const headers = computed(() => {
  const parsedHeaders = [
    {
      name: 'Name',
      key: 'name',
    },
    {
      name: 'Email',
      key: 'email',
    },
    {
      name: 'Role',
      key: 'role',
    },
  ];

  if (showDetails.value) {
    parsedHeaders.push(
      {
        name: 'Created On',
        key: 'createdOn',
      },
      {
        name: 'Updated On',
        key: 'updatedOn',
      },
    );
  }

  return parsedHeaders;
})
</script>

<template>
  <div class="py-8 px-12 max-w-[1440px] flex mx-auto">
    <div class="flex flex-col text-slate-700">
      <h1 class="font-semibold text-4xl">Users</h1>
      <div class="flex justify-between mb-4">
        <div class="mb-4 mt-2">{{ users.length }} users shown</div>
        <div class="flex items-center">
          <label for="showDetails">Show Details</label>
          <input type="checkbox" v-model="showDetails" id="showDetails" class="ml-2" />
        </div>
      </div>
      <div class="flex">
        <Table :headers="headers" :items="users">
          <template #cell:name="{ item }">
            {{ item.name }}
          </template>
          <template #cell:email="{ item }">
            {{ item.email }}
          </template>
          <template #cell:role="{ item }">
            {{ item.role }}
          </template>
          <template #cell:createdOn="{ item }">
            {{ formatDateTime(new Date(item.createdOn)) }}
          </template>
          <template #cell:updatedOn="{ item }">
            {{ formatDateTime(new Date(item.updatedOn)) }}
          </template>
          <template #empty>
            No users found
          </template>
        </Table>

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

<style scoped>
</style>
