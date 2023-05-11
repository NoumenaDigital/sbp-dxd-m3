<script setup lang="ts">
import SuccessIcon from "@/components/icons/SuccessIcon.vue";
import FailIcon from "@/components/icons/FailIcon.vue";

const props = defineProps<{
  successful: boolean;
}>();
</script>
<template>
  <div class="status-card" v-if="props.successful">
    <SuccessIcon/>
    <h4>Success!</h4>
    <span>The invoice has been verified and matches our records.</span>
  </div>
  <div class="status-card" v-else>
    <FailIcon/>
    <h3>Verfication Failure!</h3>
    <span>The invoice does not match our records.</span>
  </div>
</template>
<style scoped lang="scss">
.status-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .success-icon,
  .fail-icon {
    margin-top: 28px;
    margin-bottom: 8px;
  }

  span {
    font-style: normal;
    font-weight: 400;
    font-size: 16px;
    line-height: 19px;
    color: var(--primary-color);
  }
}

h4 {
  font-style: normal;
  font-weight: 700;
  font-size: 16px;
  line-height: 19px;
  color: var(--primary-color);
  margin: 8px 0;
}
</style>
