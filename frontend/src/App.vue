<script setup lang="ts">
import { RouterView } from "vue-router";
</script>

<template>
  <RouterView />
</template>

<style>
* {
  margin: 0;
}

html,
body {
  height: 100%;
}

#app {
  min-height: 100%;
  height: 100%;
  font-family: "Roboto", sans-serif;
}

a {
  font-weight: 400;
  font-size: 14px;
  line-height: 16px;
  /*text-decoration-line: underline;*/
  color: var(--primary-color);
}

sl-button::part(base) {
  width: 209px;
  height: 48px;
  background-color: var(--primary-color-lightest);
  color: var(--white);
  border-radius: 4px;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 48px;
}
.hidden {
  display: none !important;
}
.column-section {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}
.row-section {
  display: flex;
  flex-direction: row;
  flex-grow: 1;
}
</style>
