<script setup lang="ts">
export interface Props {
  label: string;
  type?: string;
  modelValue?: string | number | undefined | null;
  disabled?: boolean;
  autofocus?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: "text",
  disabled: false,
  autofocus: false,
});
</script>
<template>
  <div class="input-component">
    <sl-input
        class="label-on-left"
        :label="props.label"
        :type="props.type"
        :value="modelValue"
        :disabled="disabled"
        :autofocus="autofocus"
        :required="true"
        @input="$emit('update:modelValue', $event.target.value)"
    ></sl-input>
  </div>
</template>
<style scoped lang="scss">
sl-input::part(form-control-label) {
  font-weight: 400;
  font-size: 14px;
  line-height: 16px;
  color: var(--primary-color);
}

sl-input::part(base) {
  border: none;
  border-bottom: solid 1px var(--black);
  border-radius: 0;
  box-shadow: none !important;
  background-color: transparent;
}

.input-component {
  flex-grow: 1;
  max-height: 60px;
}
</style>
