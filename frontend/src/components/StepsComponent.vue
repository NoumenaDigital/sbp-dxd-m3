<script setup lang="ts">
import CheckIcon from "@/components/icons/CheckIcon.vue";

const props = defineProps<{
  steps: number;
  activeStep: number;
}>();
</script>
<template>
  <div class="steps-container">
    <template v-for="n in steps" class="step-section">
      <span
          class="step"
          :class="{ active: n === activeStep, finished: n < activeStep }"
      >
        <CheckIcon v-if="activeStep > n"/>
        <template v-else>{{ n }}</template>
      </span>
      <div class="step-buffer" v-if="n !== steps">
        <sl-divider></sl-divider>
      </div>
    </template>
  </div>
</template>
<style scoped lang="scss">
.steps-container {
  width: 100%;
  max-width: 520px;
  display: flex;
  margin: auto;

  .step-buffer {
    flex-grow: 1;

    sl-divider {
      margin: 20px 5px;
    }
  }

  .step {
    width: 40px;
    height: 40px;
    border-radius: 20px;
    flex-shrink: 0;
    background-color: rgba(21, 70, 129, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 22px;
    line-height: 26px;
    color: var(--white);

    &.finished,
    &.active {
      background-color: var(--primary-color);
    }
  }
}
</style>
