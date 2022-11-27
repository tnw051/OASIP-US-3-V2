<script setup>
import { defineComponent } from "vue";
import {Field, ErrorMessage} from "vee-validate";
import FieldLabel from "./FieldLabel.vue";

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
  required: {
    type: Boolean,
    required: true,
  },
  useFieldSlot: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
});
</script>

<script>
export default defineComponent({
  name: "InputField",
  extends: Field,
  inheritAttrs: false,
});
</script>
 
<template>
  <div class="flex flex-col gap-2">
    <FieldLabel 
      :label="props.label"
      :name="props.name"
      :required="props.required"
    />
    <div>
      <Field
        v-if="!useFieldSlot"
        :id="name"
        :name="name"
        :required="required"
        :as="as"
        v-bind="{...$attrs, ...$props}"
        class="w-full rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <Field
        v-else
        :id="name"
        :name="name"
        :required="required"
        :as="as"
        v-bind="{...$attrs, ...$props}"
        class="w-full rounded-md border border-slate-500/10 bg-slate-500/5 p-2 px-3 text-slate-800 focus:border-transparent focus:outline-none focus:ring-2 focus:ring-blue-500"
      >
        <slot />
      </Field>
      <ErrorMessage
        v-if="!error"
        :name="name"
        class="block rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
      />
      <div
        v-else
        class="block rounded bg-red-500/5 p-1 pl-3 text-sm text-red-500"
      >
        {{ error }}
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.required::after {
  content: '*';
  @apply text-red-500 pl-1
}
</style>