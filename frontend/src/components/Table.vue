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
</script>
 
<template>
  <table
    class="table-fixed text-left w-8/12 flex-1 break-words border border-slate-200 shadow-xl shadow-black/5 p-4 h-full"
  >
    <thead class="text-xs text-slate-500 uppercase bg-slate-100 text-left">
      <tr>
        <th
          v-for="header in headers"
          :key="header"
          class="pl-2 py-3"
        >
          {{ header.name }}
        </th>
        <th
          v-if="enableActions"
          class="pl-2 py-3"
        >
          Actions
        </th>
      </tr>
    </thead>

      
    <template v-if="items.length > 0">
      <tr
        v-for="item in items"
        :key="keyExtractor(item)"
        class=" my-10 bg-white rounded-lg border-b border-gray-200 shadow-black/5 relative hover:bg-gray-50 transition box-border"
        :class="[
          {
            'z-10 bg-blue-200/10 hover:bg-blue-200/20 ring-2 ring-blue-400/50 ':
              props.keyExtractor(item) === props.selectedKey,
          }
        ]"
        @click="emits('select', item)"
      >
        <td
          v-for="header in headers"
          :key="header"
          class="py-2 px-2"
        >
          <slot
            :name="`cell:${header.key}`"
            :item="item"
          />
        </td>

        <td
          v-if="enableActions"
          class="py-2 px-2"
        >
          <div class="flex">
            <button
              v-if="props.enableEdit"
              class="text-slate-400 hover:text-yellow-500 disabled:hover:text-slate-400 text-xs flex items-center justify-center w-8 h-8 rounded-full transition"
              :disabled="props.selectedItem"
              @click.stop="emits('edit', item)"
            >
              <span class="material-symbols-outlined">
                edit
              </span>
            </button>

            <button
              v-if="props.enableDelete"
              class="text-slate-400 hover:text-red-500 disabled:hover:text-slate-400 text-xs flex items-center justify-center w-8 h-8 rounded-full transition"
              :disabled="props.selectedItem"
              @click.stop="emits('delete', item)"
            >
              <span class="material-symbols-outlined">
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
</style>