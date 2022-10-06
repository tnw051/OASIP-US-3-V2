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
    class="h-full w-8/12 flex-1 table-fixed break-words border border-slate-200 p-4 text-left shadow-xl shadow-black/5"
  >
    <thead class="bg-slate-100 text-left text-xs uppercase text-slate-500">
      <tr>
        <th
          v-for="header in headers"
          :key="header"
          class="py-3 pl-2"
        >
          {{ header.name }}
        </th>
        <th
          v-if="enableActions"
          class="py-3 pl-2"
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
            'z-10 bg-blue-200/10 hover:bg-blue-200/20 ring-2 ring-blue-400/50 ':
              props.keyExtractor(item) === props.selectedKey,
          }
        ]"
        @click="emits('select', item)"
      >
        <td
          v-for="header in headers"
          :key="header"
          class="p-2"
        >
          <slot
            :name="`cell:${header.key}`"
            :item="item"
          />
        </td>

        <td
          v-if="enableActions"
          class="p-2"
        >
          <div class="flex">
            <button
              v-if="props.enableEdit"
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs text-slate-400 transition hover:text-yellow-500 disabled:hover:text-slate-400"
              :disabled="props.selectedItem"
              @click.stop="emits('edit', item)"
            >
              <span class="material-symbols-outlined">
                edit
              </span>
            </button>

            <button
              v-if="props.enableDelete"
              class="flex h-8 w-8 items-center justify-center rounded-full text-xs text-slate-400 transition hover:text-red-500 disabled:hover:text-slate-400"
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