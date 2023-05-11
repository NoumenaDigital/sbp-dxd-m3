<script setup lang="ts">
import InputComponent from "@/components/InputComponent.vue";
import {ref} from "vue";
import {useUserStore} from "@/stores/user";

const {login} = useUserStore();

const username = ref("");
const password = ref("");
const errorMessage = ref("");

function submit() {
  errorMessage.value = "";
  login(username.value, password.value).catch(() => {
    errorMessage.value = "Login failed. Try again";
  });
}
</script>
<template>
  <div class="login-component">
    <InputComponent label="Username" v-model="username" :autofocus="true"/>
    <InputComponent label="Password" type="password" v-model="password"/>
    <span v-if="errorMessage" class="error-message">{{ errorMessage }}</span>
    <sl-button variant="primary" @click="submit()">Login</sl-button>
  </div>
</template>

<style scoped lang="scss">
.login-component {
  width: 400px;
  height: 302px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  background: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;

  .input-component {
    width: 330px;
  }

  a {
    width: 330px;
  }
}

.error-message {
  color: var(--warning);
}
</style>
