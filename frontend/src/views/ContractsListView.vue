<script setup lang="ts">
import TableContractComponent from "@/components/TableContractComponent.vue";
import { computed, onMounted } from "vue";
import { useContractStore } from "@/stores/contract";

const contract = useContractStore();

const contractsList = computed(() => {
  return contract.getContracts;
});

const tableHeader = [
  "Contract Name",
  "Value",
  "Customer",
  "Date Created",
  "Completion",
];
onMounted(() => {
  contract.loadContracts();
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>My Contracts</h1>
      </div>
      <sl-button variant="primary" @click="$router.push('/new-contract')"
        >Add New Contract
      </sl-button>
    </div>
    <TableContractComponent :header="tableHeader" :data="contractsList" />
  </div>
</template>
<style scoped lang="scss">
.dashboard-page {
  display: flex;

  .main-container {
    background-color: var(--light-gray-97);
    flex-grow: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: 100vh;
  }

  .main-content {
    flex-grow: 1;
    padding: 24px 15px 15px 30px;
    overflow: scroll;
  }
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;

  sl-button {
    align-self: end;
  }

  left-section {
    display: flex;
    align-items: center;
  }
}

h1 {
  font-weight: 700;
  font-size: 22px;
  line-height: 26px;
  color: var(--primary-color);
  margin-bottom: 4px;
}


</style>
