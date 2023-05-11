<script setup lang="ts">
import {useRoute} from "vue-router";

import {useUserStore} from "@/stores/user";

const route = useRoute();
const {logout, getParty} = useUserStore();

const routeName: string = route.name?.toString() || "";
</script>
<template>
  <div class="side-bar">
    <sl-tab-group placement="start" class="main-menu">
      <sl-tab
          slot="nav"
          :active="routeName.includes('contract')"
          @click="$router.push('/contracts')"
      >Contracts
      </sl-tab
      >
      <sl-tab
          slot="nav"
          :active="routeName.includes('invoice')"
          @click="$router.push('/invoices')"
      >Invoices
      </sl-tab
      >
      <sl-tab
          slot="nav"
          :active="route.name === 'offers'"
          @click="$router.push('/offers')"
      >Offers
      </sl-tab
      >
      <sl-tab
          slot="nav"
          :active="route.name === 'marketplace'"
          @click="$router.push('/marketplace')"
      >Marketplace
      </sl-tab
      >
      <sl-tab
          slot="nav"
          :active="route.name === 'portfolio'"
          @click="$router.push('/portfolio')"
      >Portfolio
      </sl-tab
      >
    </sl-tab-group>
    <sl-divider></sl-divider>
    <sl-tab-group placement="start" class="bottom-menu">
      <sl-tab slot="nav" @click="logout()">Logout</sl-tab>
    </sl-tab-group>
  </div>
</template>
<style scoped lang="scss">
.side-bar {
  flex-grow: 0;
  flex-shrink: 0;
  background-color: var(--primary-color);
  top: 0;
  left: 0;
  width: 225px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  sl-tab-group.main-menu {
    margin-top: 24px;
    flex-grow: 1;
    --track-color: var(--primary-color);
    --indicator-color: var(--light-blue);
  }

  sl-tab-group.bottom-menu {
    --track-color: var(--primary-color);
    --indicator-color: var(--primary-color);
  }

  sl-tab-group::part(base) {
    --track-width: 10px;
    flex-direction: column;
    margin-right: -1px;
    margin-left: -1px;
  }

  sl-tab::part(base) {
    color: var(--white);
    font-style: normal;
    font-weight: 500;
    font-size: 18px;
    line-height: 21px;
    padding-left: 15px;
  }
}
</style>
