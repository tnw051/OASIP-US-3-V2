<script setup>
import { computed } from "vue";

const props = defineProps({
  headers: {
    type: Array,
    required: true,
  },
  items: {
    type: Array,
    default: () => [],
  },
  enableEdit: {
    type: Boolean,
    default: false,
  },
  enableDelete: {
    type: Boolean,
    default: false,
  },
  selectedKey: {
    default: null,
    type: String,
  },
  keyExtractor: {
    type: Function,
    default: (item) => item,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
});

const emits = defineEmits(["edit", "delete", "select"]);
const enableActions = computed(() => props.enableEdit || props.enableDelete);

// let user pass <td> to slot instead of content inside <td>
</script>
 
<template>
  <table
    class="h-full flex-1 table-fixed border-separate border-spacing-0 break-words rounded-lg  border border-slate-100 bg-white p-4 text-left shadow-xl shadow-black/5"
  >
    <thead class="text-left text-xs uppercase text-slate-500">
      <tr>
        <th
          v-for="header in headers"
          :key="header"
          class="border-b border-slate-100 py-4 px-2"
        >
          {{ header.name }}
        </th>
        <th
          v-if="enableActions"
          class="border-b border-slate-100 py-4 px-2"
        >
          Actions
        </th>
      </tr>
    </thead>


    <template v-if="items.length > 0">
      <tr
        v-for="item in items"
        :key="keyExtractor(item)"
        class=" relative my-10 box-border rounded-lg border-b border-gray-200 bg-white shadow-black/5 transition hover:bg-gray-50"
        :class="[
          {
            'z-10 bg-blue-200/10 hover:bg-blue-200/20 ring-1 ring-blue-400/50 ':
              props.keyExtractor(item) === props.selectedKey,
          }
        ]"
        @click="emits('select', item)"
      >
        <slot
          v-for="(header, headerIndex, ) in headers"
          :key="header"
          :name="`cell:${header.key}`"
          :item="item"
          :d-class="['px-2 py-2', { 'py-3': headerIndex === 0 }]"
        />

        <td
          v-if="enableActions"
          class="p-2 pr-4"
        >
          <div class="material-icon-settings flex">
            <button
              v-if="props.enableEdit"
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs text-slate-400 transition hover:text-yellow-500 disabled:hover:text-slate-400"
              :disabled="props.selectedItem"
              @click.stop="emits('edit', item)"
            >
              <span
                class="material-symbols-outlined"
                title="Edit"
              >
                edit
              </span>
            </button>

            <button
              v-if="props.enableDelete"
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs text-slate-400 transition hover:text-red-500 disabled:hover:text-slate-400"
              :disabled="props.selectedItem"
              @click.stop="emits('delete', item)"
            >
              <span
                class="material-symbols-outlined"
                title="Delete"
              >
                delete
              </span>
            </button>
          </div>
        </td>
      </tr>
    </template>
    <tr v-else>
      <td
        class="p-6 text-center"
        :colspan="headers.length + (enableActions ? 1 : 0)"
      >
        <slot
          v-if="props.isLoading"
          name="loading"
        >
          Loading...
        </slot>
        <slot
          v-else
          name="empty"
        >
          No data
        </slot>
      </td>
    </tr>
  </table>
</template>
 
<style scoped>
.material-icon-settings {
  font-variation-settings:
    'FILL' 0,
    'wght' 400,
    'GRAD' 0,
    'opsz' 48;
}

.material-symbols-outlined {
  font-size: 1.4rem;
}
</style>