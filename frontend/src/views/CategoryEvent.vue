<script setup>
import { onBeforeMount, ref } from "vue";
import EditCategory from "../components/EditCategory.vue";
import Modal from "../components/Modal.vue";
import Table from "../components/Table.vue";
import { getCategories, updateCategory } from "../service/api";

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
        <Table :headers="[
          {
            name: 'Category Name',
            key: 'name',
          },
          {
            name: 'Description',
            key: 'description',
          },
          {
            name: 'Duration',
            key: 'duration',
          },
        ]" :items="categories" enable-edit @edit="startEdit" :selected-key="currentCategory.id"
          :key-extractor="(category) => category.id">
          <template #cell:name="{ item }">
            <span class="font-medium">{{ item.eventCategoryName }}</span>
          </template>

          <template #cell:description="{ item }">
            <span class="font-medium">{{ item.eventCategoryDescription }}</span>
          </template>

          <template #cell:duration="{ item }">
            <span class="font-medium">{{ item.eventDuration }}</span>
          </template>

          <template #empty>
            No categories found
          </template>
        </Table>

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
