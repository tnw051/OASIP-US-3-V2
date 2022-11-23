<script setup lang="ts">
import { onBeforeMount, ref } from "vue";
import BaseTable from "../components/BaseTable.vue";
import EditCategory from "../components/EditCategory.vue";
import Modal from "../components/Modal.vue";
import PageLayout from "../components/PageLayout.vue";
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
  <PageLayout header="Categories">
    <template #subheader>
      <div class="mb-4 mt-2">
        {{ categories.length }} events shown
      </div>
    </template>
    <template #content>
      <div class="flex">
        <BaseTable
          :headers="[
            {
              name: 'Category Name',
              key: 'name',
            },
            {
              name: 'Description',
              key: 'description',
              override: true,
            },
            {
              name: 'Duration (min.)',
              key: 'duration',
            },
          ]"
          :items="categories"
          enable-edit
          :selected-key="editingState.isEditing && editingState.item?.id.toString()"
          :key-extractor="(category) => category.id"
          @edit="startEditing"
        >
          <template #cell:name="{ item }: SlotProps">
            <span class="font-medium">{{ item.eventCategoryName }}</span>
          </template>

          <template #cell:description="{ item, dClass }: SlotProps">
            <td
              :class="dClass"
              class="w-6/12"
            >
              <span class="font-medium">{{ item.eventCategoryDescription }}</span>
            </td>
          </template>

          <template #cell:duration="{ item }: SlotProps">
            <span class="font-medium">{{ item.eventDuration }}</span>
          </template>

          <template #empty>
            No categories found
          </template>
        </BaseTable>

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
    </template>
  </PageLayout>

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
