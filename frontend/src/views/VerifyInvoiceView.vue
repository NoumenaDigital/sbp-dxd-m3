<script setup lang="ts">
import VerifyInvoiceComponent from "../components/VerifyInvoiceComponent.vue";
</script>
<template>
  <div class="verify-invoice-view">
    <VerifyInvoiceComponent />
  </div>
</template>
<style scoped lang="scss">
.verify-invoice-view {
  height: 100%;
  background-color: var(--primary-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
</style>
