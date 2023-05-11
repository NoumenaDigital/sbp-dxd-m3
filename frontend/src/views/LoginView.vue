<script setup lang="ts">
import LoginComponent from "../components/LoginComponent.vue";
</script>
<template>
  <div class="verify-invoice-view">
    <h1 class="login-title">Smart Billing Platform</h1>
    <LoginComponent />
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

  .login-title {
    font-size: 48px;
    color: var(--white);
    margin-bottom: 40px;
  }
}
</style>
