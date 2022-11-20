<script setup lang="ts">
import { onBeforeMount, ref } from "vue";
import EditCategory from "../components/EditCategory.vue";
import Modal from "../components/Modal.vue";
import Table from "../components/Table.vue";
import { CategoryResponse, EditCategoryRequest } from "../gen-types";
import { getCategories, updateCategory } from "../service/api";
import { BaseSlotProps } from "../types";
import { useEditing } from "../utils/useEditing";

const categories = ref<CategoryResponse[]>([]);
const { editingState, startEditing, stopEditing } = useEditing<CategoryResponse>();

onBeforeMount(async () => {
  const _categories = await getCategories();
  // sort by their ids in ascending order
  _categories.sort((a, b) => a.id - b.id);
  categories.value = _categories;
});

const isEditSuccessModalOpen = ref(false);
const isEditErrorModalOpen = ref(false);

async function saveCategory(newValues: EditCategoryRequest) {
  if(!editingState.isEditing) {
    return;
  }
  
  const currentCategory = editingState.item;
  const categoryId = currentCategory.id;
  const updates: EditCategoryRequest = {};

  if (newValues.eventCategoryName !== currentCategory.eventCategoryName) {
    updates.eventCategoryName = newValues.eventCategoryName;
  }
  if (newValues.eventCategoryDescription !== currentCategory.eventCategoryDescription) {
    updates.eventCategoryDescription = newValues.eventCategoryDescription;
  }
  if (newValues.eventDuration !== currentCategory.eventDuration) {
    updates.eventDuration = newValues.eventDuration;
  }

  if (Object.keys(updates).length > 0) {
    const updatedCategory = await updateCategory(categoryId, updates);
    if (updatedCategory) {
      const category = categories.value.find((c) => c.id === categoryId);
      if (category) {
        Object.assign(category, updatedCategory);
      }
      isEditSuccessModalOpen.value = true;
    } else {
      isEditErrorModalOpen.value = true;
    }
  }

  stopEditing();
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
type SlotProps = BaseSlotProps<CategoryResponse>;
</script>

<template>
  <div class="mx-auto flex max-w-[1440px] py-8 px-12">
    <div class="flex flex-col text-slate-700">
      <h1 class="text-4xl font-semibold">
        Categories
      </h1>
      <div class="mb-4 flex justify-between">
        <div class="mb-4 mt-2">
          {{ categories.length }} events shown
        </div>
      </div>
      <div class="flex">
        <Table
          :headers="[
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
          ]"
          :items="categories"
          enable-edit
          :selected-key="editingState.item?.id.toString()"
          :key-extractor="(category) => category.id"
          @edit="startEditing"
        >
          <template #cell:name="{ item }: SlotProps">
            <span class="font-medium">{{ item.eventCategoryName }}</span>
          </template>

          <template #cell:description="{ item }: SlotProps">
            <span class="font-medium">{{ item.eventCategoryDescription }}</span>
          </template>

          <template #cell:duration="{ item }: SlotProps">
            <span class="font-medium">{{ item.eventDuration }}</span>
          </template>

          <template #empty>
            No categories found
          </template>
        </Table>

        <div
          v-if="editingState.isEditing"
          class="relative w-4/12 bg-slate-100 p-4"
        >
          <EditCategory
            v-if="editingState.isEditing"
            class="sticky top-24"
            :category="editingState.item"
            :categories="categories"
            @cancel="stopEditing"
            @save="saveCategory"
          />
        </div>
      </div>
    </div>
  </div>

  <Modal
    title="Success"
    subtitle="Category has been saved"
    :is-open="isEditSuccessModalOpen"
    @close="isEditSuccessModalOpen = false"
  />

  <Modal
    title="Error"
    subtitle="Something went wrong"
    button-text="Try Again"
    :is-open="isEditErrorModalOpen"
    variant="error"
    @close="isEditErrorModalOpen = false"
  />
</template>

<style>

</style>
