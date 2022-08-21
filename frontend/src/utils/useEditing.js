import { ref } from "vue";

export function useEditing(defaultValue = null) {
  const isEditing = ref(false);
  const editingItem = ref(defaultValue);

  function startEditing(item) {
    if (isEditing.value) {
      return;
    }
    isEditing.value = true;
    editingItem.value = item;
  }

  function stopEditing() {
    isEditing.value = false;
    editingItem.value = defaultValue;
  }

  function withNoEditing(callback) {
    if (!isEditing.value) {
      callback();
    }
  }

  return {
    isEditing,
    editingItem,
    startEditing,
    stopEditing,
    withNoEditing,
  };
}