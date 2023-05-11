<script setup lang="ts">
import TableInvoiceComponent from "@/components/TableInvoiceComponent.vue";
import { useInvoiceStore } from "@/stores/invoice";
import { computed, onMounted } from "vue";

const invoice = useInvoiceStore();

const invoicesList = computed(() => {
  return invoice.getInvoices;
});

const tableHeader = [
  "Invoice ID",
  "Value",
  "Customer",
  "Creation Date",
  "Payment Expected",
  "Status",
];

onMounted(() => {
  invoice.loadInvoices();
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>My Invoices</h1>
      </div>
      <sl-button variant="primary" @click="$router.push('/new-invoice')"
        >Add New Invoice
      </sl-button>
    </div>
    <TableInvoiceComponent :header="tableHeader" :data="invoicesList" />
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
