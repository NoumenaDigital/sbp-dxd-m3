<script setup lang="ts">
import SuccessIcon from "@/components/icons/SuccessIcon.vue";
import ArrowUpIcon from "@/components/icons/ArrowUpIcon.vue";
import ArrowDownIcon from "@/components/icons/ArrowDownIcon.vue";

const props = defineProps<{
  title: string;
  description: string;
  value: string;
  period: string;
  up: boolean;
}>();
</script>
<template>
  <div class="banner-card">
    <SuccessIcon/>
    <div class="info">
      <h3>{{ props.title }}</h3>
      <span class="description">{{ props.description }}</span>
      <div class="value">
        <ArrowUpIcon v-if="props.up === true"/>
        <ArrowDownIcon v-else/>
        <span class="progress" :class="{ raise: props.up, fall: !props.up }">
          {{ props.value }}%
        </span>
        <span> this {{ props.period }}</span>
      </div>
    </div>
  </div>
</template>
<style scoped lang="scss">
.banner-card {
  max-width: 500px;
  height: 120px;
  background-color: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;
  display: flex;
  flex-grow: 1;
  align-items: center;

  .success-icon {
    margin: 0 40px 0 24px;

    path {
      fill: var(--primary-color-darker);
    }
  }

  .info {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
  }

  .progress {
    font-weight: 700;
    font-size: 14px;
    line-height: 16px;
  }

  .description {
    font-weight: 400;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
    text-transform: uppercase;
  }

  .value {
    margin-top: 16px;
  }

  .raise {
    color: var(--success);
  }

  .fall {
    color: var(--warning);
  }

  .arrow {
    margin-right: 7px;
  }
}

h3 {
  font-weight: 500;
  font-size: 18px;
  line-height: 21px;
  color: var(--primary-color);
}
</style>
