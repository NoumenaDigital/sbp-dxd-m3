<script setup lang="ts">
import {commonCurrencies} from "@/utils/helpers/various";
import {ref} from "vue";

interface Props {
  modelValue?: string | undefined | null;
  disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
});
const currencies = ref(commonCurrencies);
</script>
<template>
  <div class="select-container">
    <label for="ccy-select">CCY*</label>
    <div>
      <select
          id="ccy-select"
          :disabled="disabled"
          v-model="modelValue"
          @change="
          $emit('update:modelValue', ($event.target as HTMLInputElement).value)
        "
      >
        <option
            v-for="currency in Object.keys(currencies)"
            :value="currency"
            :key="currency"
        >
          {{ currency }} - {{ currencies[currency].name }}
        </option>
      </select>
    </div>
  </div>
</template>
<style scoped lang="scss">
.select-container {
  display: flex;
  box-sizing: border-box;
  flex-direction: column;
  align-items: start;

  label {
    margin: 2px 0px 2px 0px;
    font-weight: 400;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
  }

  select {
    background: transparent;
    height: 40px;
    border: none;
    border-bottom: solid 1px;
    width: 52px;
  }
}
</style>
