<script setup lang="ts">
import { computed, onBeforeMount, ref } from "vue";
import BaseTable from "../components/BaseTable.vue";
import EditUser from "../components/EditUser.vue";
import Modal from "../components/Modal.vue";
import PageLayout from "../components/PageLayout.vue";
import {
  CategoryOwnerResponse,
  CategoryResponse,
  EditUserRequest,
  Role,
  UserResponse,
} from "../gen-types";
import {
  deleteUser,
  getCategories,
  getRoles,
  getUsers,
  updateUser,
} from "../service/api";
import { getCategoryOwners } from "../service/api/category-owners";
import { BaseSlotProps } from "../types";
import { formatDateTime } from "../utils/index";
import { useEditing } from "../utils/useEditing";

const users = ref<UserResponse[]>([]);
const roles = ref<Role[]>([]);
const categoryOwners = ref<CategoryOwnerResponse[]>([]);
const categories = ref<CategoryResponse[]>([]);
const showDetails = ref(false);
const isLoggedIn = ref(true);
const { editingState, startEditing, stopEditing } = useEditing<UserResponse>();

// email -> categories
const ownershipMap = computed(() => {
  if (!categoryOwners.value || !categories.value) {
    return;
  }
  const map = new Map<string, CategoryResponse[]>();
  const categoryIdToCategoryMap = new Map<number, CategoryResponse>();
  categories.value.forEach((c) => {
    categoryIdToCategoryMap.set(c.id, c);
  });

  categoryOwners.value.forEach((co) => {
    const categories = map.get(co.ownerEmail) || [];
    categories.push(categoryIdToCategoryMap.get(co.eventCategoryId));
    map.set(co.ownerEmail, categories);
  });

  return map;
});

// categoryId -> owners
const categoryOwnerMap = computed(() => {
  if (!categoryOwners.value || !categories.value) {
    return;
  }
  const map = new Map<number, CategoryOwnerResponse[]>();
  categoryOwners.value.forEach((co) => {
    const owners = map.get(co.eventCategoryId) || [];
    owners.push(co);
    map.set(co.eventCategoryId, owners);
  });

  return map;
});

// if the user is lecturer, check if there are any lecturers left to take over the categories (owners of each category must be > 0)
// if there are none, throw an error

onBeforeMount(async () => {
  users.value = (await getUsers({
    onUnauthorized: () => {
      isLoggedIn.value = false;
    },
  })) || [];
  roles.value = (await getRoles()) || [];
  categories.value = (await getCategories()) || [];
  categoryOwners.value = (await getCategoryOwners()) || [];
});

const headers = computed(() => {
  const parsedHeaders = [
    {
      name: "Name",
      key: "name",
    },
    {
      name: "Email",
      key: "email",
    },
    {
      name: "Role",
      key: "role",
    },
  ];

  if (showDetails.value) {
    parsedHeaders.push(
      {
        name: "Created On",
        key: "createdOn",
      },
      {
        name: "Updated On",
        key: "updatedOn",
      },
    );
  }

  return parsedHeaders;
});

async function confirmDeleteUser(user: UserResponse) {
  const hasOwnership = ownershipMap.value.has(user.email);
  if (hasOwnership) {
    // ensure that the user is not the only owner of any category
    const ownCategories = ownershipMap.value.get(user.email);
    const ownCategoriesWithOneOwner = ownCategories.filter((c) => {
      const owners = categoryOwnerMap.value.get(c.id);
      return owners.length === 1;
    });

    const denyMessage = `${user.name} is the owner of ${getCategoriesString(ownCategories)}. \n\nYou cannot delete this user account since ${user.name} is the only owner of ${getCategoriesString(ownCategoriesWithOneOwner)}. \n\nAnother owner must be added to the event category(s) before this lecturer can be deleted.`;
    const confirmMessage = `${user.name} is the owner of ${getCategoriesString(ownCategories)}. Deletion of this user account will also remove this user from the event category(s). Do you still want to delete this account?`;
    if (ownCategoriesWithOneOwner.length > 0) {
      alert(denyMessage);
      return;
    } else if (!confirm(confirmMessage)) {
      return;
    }
  } else if (!confirm(`Are you sure you want to delete ${user.name} (${user.email})?`)) {
    return;
  }
  const isSuccess = await deleteUser(user.id);
  if (isSuccess) {
    users.value = users.value.filter((u) => u.id !== user.id);
    alert("User deleted successfully");
  } else {
    alert("Failed to delete user");
  }
}

function getCategoriesString(categories: CategoryResponse[]) {
  let categoriesString = "";
  for (let i = 0; i < categories.length; i++) {
    categoriesString += categories[i].eventCategoryName;
    if (i !== categories.length - 1) {
      categoriesString += ", ";
    }
  }

  return categoriesString;
}


const isEditSuccessModalOpen = ref(false);
const isEditErrorModalOpen = ref(false);

async function saveUser(updates: EditUserRequest) {
  if (!editingState.isEditing) {
    return;
  }
  const currentUser = editingState.item;
  const updatedUser = await updateUser(currentUser.id, updates);
  if (updatedUser) {
    const user = users.value.find((u) => u.id === updatedUser.id);
    if (!user) {
      return;
    }

    Object.assign(user, updatedUser);
    isEditSuccessModalOpen.value = true;
  } else {
    isEditErrorModalOpen.value = true;
  }

  stopEditing();
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
type SlotProps = BaseSlotProps<UserResponse>;
</script>

<template>
  <PageLayout header="Users">
    <template #subheader>
      <div class="mb-4 mt-2 flex justify-between">
        <div>
          {{ users.length }} users shown
        </div>
        <div class="flex items-center gap-2">
          <input
            id="showDetails"
            v-model="showDetails"
            type="checkbox"
            class="ml-2"
          >
          <label for="showDetails">Show Details</label>
        </div>
      </div>
    </template>
    <template #content>
      <div class="flex">
        <BaseTable
          :headers="headers"
          :items="users"
          enable-edit
          enable-delete
          :selected-key="editingState.isEditing && editingState.item?.id.toString()"
          :key-extractor="(user) => user.id"
          @edit="startEditing"
          @delete="confirmDeleteUser"
        >
          <template #cell:name="{ item }: SlotProps">
            {{ item.name }}
          </template>
          <template #cell:email="{ item }: SlotProps">
            {{ item.email }}
          </template>
          <template #cell:role="{ item }: SlotProps">
            {{ item.role }}
          </template>
          <template #cell:createdOn="{ item }: SlotProps">
            {{ formatDateTime(new Date(item.createdOn)) }}
          </template>
          <template #cell:updatedOn="{ item }: SlotProps">
            {{ formatDateTime(new Date(item.updatedOn)) }}
          </template>
          <template #empty>
            <span v-if="isLoggedIn">
              No users found
            </span>
            <span v-else>
              Please <router-link
                to="/login"
                class="text-sky-500 underline"
              >login</router-link> to view users
            </span>
          </template>
        </BaseTable>

        <div
          v-if="editingState.isEditing"
          class="relative w-4/12 bg-slate-100 p-4"
        >
          <EditUser
            v-if="editingState.isEditing"
            class="sticky top-24"
            :current-user="editingState.item"
            :roles="roles"
            @cancel="stopEditing"
            @save="saveUser"
          />
        </div>
      </div>
    </template>
  </PageLayout>

  <Modal
    title="Success"
    subtitle="User has been saved"
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

<style scoped>

</style>
