<script setup lang="ts">
import TableMarketplaceComponent from "@/components/TableMarketplaceComponent.vue";
import { useOfferStore } from "@/stores/offer";
import { computed, onMounted } from "vue";

const tableHeader = [
  "Offer",
  "Price",
  "Amount",
  "Supplier",
  "Debtor",
  "Due date",
];

const offer = useOfferStore();

const offersList = computed(() => {
  return offer.getOffers;
});

onMounted(() => {
  offer.loadMarketplaceOffers();
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>Marketplace</h1>
      </div>
    </div>

    <TableMarketplaceComponent :header="tableHeader" :data="offersList" />
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
