<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { computed, onMounted } from "vue";
import { useContractStore } from "@/stores/contract";
import { formatDate, getContractStatus } from "@/utils/helpers/various";
import MilestonesComponent from "@/components/MilestonesComponent.vue";

const contract = useContractStore();

const route = useRoute();
const router = useRouter();

const contractData = computed(() => {
  return contract.getCurrentContract;
});

function signContract() {
  contract.signContract(route.params.uuid.toString());
}

onMounted(() => {
  contract.getContract(route.params.uuid.toString());
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <div class="breadcrumb">
          <h1>Contract {{ contractData.name }}</h1>
          <span class="status" v-if="contractData.status">
            <i :class="getContractStatus(contractData.status)" />
            {{ getContractStatus(contractData.status) }}</span
          >
        </div>
      </div>
      <sl-button
        v-if="contractData.status === 'createdBySupplier'"
        variant="primary"
        @click="signContract()"
        >Sign & Submit
      </sl-button>
    </div>
    <div class="page-content">
      <div class="left-section">
        <div class="section-info">
          <div class="column-section">
            <div class="row-section">
              <div class="field">
                <span class="label">DATE CREATED</span>
                <span class="value">{{
                  formatDate(contractData.creationDate)
                }}</span>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">TOTAL GOODS VALUE</span>
                <span class="value">{{
                  contractData.goodsValue?.amount +
                    " " +
                    contractData.goodsValue?.unit || 0
                }}</span>
              </div>
            </div>
            <div class="row-section">
              <div class="field">
                <span class="label">ESTIMATED DELIVERY DATE</span>
                <span class="value">{{
                  formatDate(contractData.expectedDeliveryDate)
                }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="section-info" v-if="contractData.milestones">
          <MilestonesComponent
            v-if="contractData.uuid"
            :milestones="contractData.milestones"
            :uuid="contractData.uuid"
            :contract-status="contractData.status"
            :contract-sign-date="contractData.signDate"
          />
        </div>
      </div>
      <div class="right-section">
        <div class="section-info">
          <div class="column-section">
            <div class="row-section">
              <div class="field">
                <span class="label">SUPPLIER</span>
                <span class="value">{{
                  contractData.supplierDetails?.name || "N/A"
                }}</span>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">CUSTOMER</span>
                <span class="value">{{
                  contractData.customerDetails?.name || ""
                }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
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

  .left-section > sl-divider {
    margin: 29px 0 16px 0;
    width: 285px;
    --width: 4px;
    --color: var(--primary-color);
  }

  .right-section {
    flex-grow: 1;
  }

  sl-button {
    align-self: end;
  }
}

h1 {
  font-weight: 700;
  font-size: 22px;
  line-height: 26px;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.page-content {
  display: flex;
  justify-content: space-between;
  gap: 24px;

  .left-section {
    flex-grow: 2;
    max-width: 70%;

    .field {
      max-width: 33%;
    }
  }

  .right-section {
    flex-grow: 1;
  }

  .section-info {
    background: var(--white);
    border: 1px solid var(--light-light-gray);
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    padding: 24px;
    margin-bottom: 24px;

    .link {
      margin-top: 26px;
      font-weight: 500;
      font-size: 12px;
      line-height: 14px;
      text-transform: uppercase;
      color: var(--primary-color);
    }

    .field {
      color: var(--primary-color);
      display: flex;
      flex-direction: column;
      flex-grow: 1;

      .label {
        font-weight: 400;
        font-size: 12px;
        line-height: 14px;
      }

      .value {
        font-weight: 600;
        font-size: 16px;
        line-height: 19px;
      }
    }

    .column-section {
      gap: 24px;
    }
  }
}

.documents-section {
  background: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;
  display: flex;
  flex-direction: column;

  .header {
    border-bottom: 1px solid var(--light-light-gray);
    font-weight: 700;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
    padding: 24px;
  }

  .body {
    padding: 24px;
  }

  .message {
    font-style: italic;
    font-weight: 400;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
  }
}

.status {
  font-family: "Roboto";
  font-weight: 500;
  font-size: 12px;
  line-height: 14px;
  text-transform: uppercase;
  color: var(--primary-color);

  i {
    height: 8px;
    width: 8px;
    border-radius: 4px;
    display: inline-block;

    &.CREATED {
      background-color: var(--orange);
    }

    &.ACTIVE {
      background-color: var(--success);
    }
  }
}
</style>
