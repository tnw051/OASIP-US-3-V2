<script setup>
import { onBeforeMount, ref } from "vue";
import { getCategories, updateCategory } from "../service/api";
import EditCategory from "../components/EditCategory.vue";
import Modal from "../components/Modal.vue";

const categories = ref([]);
const currentCategory = ref({});

onBeforeMount(async () => {
  const _categories = await getCategories();
  // sort by their ids in ascending order
  _categories.sort((a, b) => a.id - b.id);
  categories.value = _categories;
});

const isEditing = ref(false)
const isEditSuccessModalOpen = ref(false);
const isEditErrorModalOpen = ref(false);

function startEdit(category) {
  if (isEditing.value) {
    return;
  }
  currentCategory.value = category;
  isEditing.value = true;
}

function stopEdit() {
  currentCategory.value = {};
  isEditing.value = false;
}

async function saveCategory(newValues) {
  const categoryId = currentCategory.value.id;
  const updates = {};

  if (newValues.eventCategoryName !== currentCategory.value.eventCategoryName) {
    updates.eventCategoryName = newValues.eventCategoryName
  }
  if (newValues.eventCategoryDescription !== currentCategory.value.eventCategoryDescription) {
    updates.eventCategoryDescription = newValues.eventCategoryDescription
  }
  if (newValues.eventDuration !== currentCategory.value.eventDuration) {
    updates.eventDuration = newValues.eventDuration
  }

  if (Object.keys(updates).length > 0) {
    const updatedCategory = await updateCategory(categoryId, updates);
    if (updatedCategory) {
      const category = categories.value.find((c) => c.id === categoryId);
      Object.assign(category, updatedCategory);
      isEditSuccessModalOpen.value = true;
    } else {
      isEditErrorModalOpen.value = true;
    }
  }

  isEditing.value = false;
  currentCategory.value = {};
}
</script>

<template>
  <div class="py-8 px-12 max-w-[1440px] flex mx-auto">
    <div class="flex flex-col text-slate-700">
      <h1 class="font-semibold text-4xl">Categories</h1>
      <div class="flex justify-between mb-4">
        <div class="mb-4 mt-2">{{ categories.length }} events shown</div>
      </div>
      <div class="flex">
        <table
          class="table-fixed text-left w-8/12 flex-1 break-words border border-slate-200 shadow-xl shadow-black/5 p-4 h-full">

          <thead class="text-xs text-slate-500 uppercase bg-slate-100 text-left">
            <tr>
              <th class="pl-2 py-3">Category Name</th>
              <th class="pl-2 py-3">Description</th>
              <th class="pl-2 py-3">Duration</th>
              <th class="pl-2 py-3">Actions</th>
            </tr>
          </thead>

          <tr v-if="categories.length > 0" v-for="category in categories"
            class=" my-10 bg-white rounded-lg border-b border-gray-200 shadow-black/5 relative hover:bg-gray-50 transition box-border"
            :class="[
              {
                'z-10 bg-blue-200/10 hover:bg-blue-200/20 ring-2 ring-blue-400/50 ':
                  currentCategory.id === category.id
              }
            ]">

            <td class="py-2 px-2">
              <span class="font-medium">{{ category.eventCategoryName }}</span>
            </td>

            <td class="py-2 px-2">
              <span class="font-medium">{{ category.eventCategoryDescription }}</span>
            </td>

            <td class="py-2 px-2">
              <span class="font-medium">{{ category.eventDuration }}</span>
            </td>

            <td class="py-2 px-2">
              <div class="flex">
                <button @click.stop="startEdit(category)"
                  class="text-slate-400 hover:text-yellow-500 disabled:hover:text-slate-400 text-xs flex items-center justify-center w-8 h-8 rounded-full transition"
                  :disabled="isEditing">
                  <span class="material-symbols-outlined">
                    edit
                  </span>
                </button>
              </div>
            </td>

          </tr>


        </table>

        <div class="p-4 bg-slate-100 relative w-4/12" v-if="currentCategory.id">
          <EditCategory class="sticky top-24" :category="currentCategory" :categories="categories" @cancel="stopEdit"
            v-if="isEditing" @save="saveCategory" />
        </div>

      </div>
    </div>
  </div>

  <Modal title="Success" subtitle="Category has been saved" :is-open="isEditSuccessModalOpen"
    @close="isEditSuccessModalOpen = false" />

  <Modal title="Error" subtitle="Something went wrong" button-text="Try Again" :is-open="isEditErrorModalOpen"
    variant="error" @close="isEditErrorModalOpen = false" />
</template>

<style>
</style>
