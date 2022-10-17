import { ref, watch } from "vue";

export function useFileInput(_placeholderFilename?: string) {
  let placeholderFilename = _placeholderFilename;
  let placeholderFile = new File([""], placeholderFilename);
  const file = ref<File | null | undefined>(null);
  const fileError = ref<string[] | false>(false);
  const fileInputRef = ref<HTMLInputElement | null>(null);

  const maxFileSize = 10 * 1024 * 1024;

  function handleFileChange(e: Event) {
    console.log("handleFileChange");
  
    const target = e.target as HTMLInputElement;
    const files = target.files;
    const selectedFile = files && files[0];

    // user cancelled file selection
    if (!selectedFile) {
    // make sure to clear the previous selected file if any
      handleRemoveFile();
      if (fileError.value) {
        fileError.value = false;
      }
      return;
    }

    if (selectedFile.size > maxFileSize) {
    // if there is no file selected before, clear the file input.
      fileError.value = [`${selectedFile.name} is too large.`, `Maximum file size is ${maxFileSize / 1024 / 1024} MB.`];
      if (!file.value) {
        handleRemoveFile();
      } else {
      // otherwise, keep the previous file selected
        const newFileList = new DataTransfer();
        const prevFile = file.value;
        newFileList.items.add(prevFile);
        if (fileInputRef.value) {
          fileInputRef.value.files = newFileList.files;
        }
        fileError.value.push(`The previous file '${prevFile.name}' is still selected.`);
      }

      return;
    }

    file.value = selectedFile;
    fileError.value = false;
  }

  function handleBlurFileInput() {
    if (fileError.value) {
      fileError.value = false;
    }
  }

  function handleRemoveFile() {
    file.value = null;
    if (fileInputRef.value) {
      fileInputRef.value.value = "";
    }
  }

  watch(fileInputRef, () => {
    if (!placeholderFilename || !fileInputRef.value) {
      return;
    }

    const fileList = new DataTransfer();
    fileList.items.add(placeholderFile);
    fileInputRef.value.files = fileList.files;
    file.value = placeholderFile;
  });

  function setPlaceholderWithName(filename: string) {
    placeholderFilename = filename;
    placeholderFile = new File([""], filename);
    if (fileInputRef.value) {
      const fileList = new DataTransfer();
      fileList.items.add(placeholderFile);
      fileInputRef.value.files = fileList.files;
      file.value = placeholderFile;
    }
  }

  // hacky for now
  function assertNoPlaceholder() {
    if (placeholderFilename && file.value === placeholderFile) {
      console.log("no change");
      file.value = undefined;
    }
  }

  return {
    file,
    fileError,
    fileInputRef,
    handleFileChange,
    handleBlurFileInput,
    handleRemoveFile,
    assertNoPlaceholder,
    setPlaceholderWithName,
  };
}