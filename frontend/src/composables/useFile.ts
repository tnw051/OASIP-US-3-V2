import { ref, watch } from "vue";

export function useFile({
  maxSize = 1024 * 1024 * 10,
} = {}) {
  const file = ref<File | null>(null);
  const fileError = ref<string | null>(null);

  function handleChangeFile(e: Event) {
    const target = e.target as HTMLInputElement;
    const files = target.files;
    if (files?.length === 0) {
      return;
    }
    if (files[0].size > maxSize) {
      fileError.value = "File size is too large";
      return;
    }
    file.value = files[0];
    fileError.value = null;
  }

  watch(file, (newFile) => {
    if (newFile) {
      console.log(`File changed: ${newFile.name}`);
    } else {
      console.log("File removed");
    }
  });      

  function handleRemoveFile() {
    file.value = null;
    fileError.value = null;
  }

  return {
    file,
    fileError,
    handleChangeFile,
    handleRemoveFile,
  };
}