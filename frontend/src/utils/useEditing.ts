import { reactive, ref, UnwrapRef } from "vue";

interface Editing<T> {
  isEditing: true;
  item: T;
}

interface NotEditing {
  isEditing: false;
  item: null;
}

type EditingState<T extends object> = Editing<T> | NotEditing;

export function useEditing<T extends object>() {
  const editingState = reactive<EditingState<T>>({
    isEditing: false,
    item: null,
  });

  return {
    editingState,
    startEditing(item: UnwrapRef<T>) {
      if (editingState.isEditing) {
        return;
      }
      Object.assign(editingState, {
        isEditing: true,
        item,
      });
    },
    stopEditing() {
      if (!editingState.isEditing) {
        return;
      }
      Object.assign(editingState, {
        isEditing: false,
        item: null,
      });
    },
    withNoEditing(callback: () => void) {
      if (!editingState.isEditing) {
        callback();
      }
    },
  };
}