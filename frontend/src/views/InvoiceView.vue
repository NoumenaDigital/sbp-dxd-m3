<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {computed, inject, onMounted} from "vue";
import {useInvoiceStore} from "@/stores/invoice";
import {formatDate} from "@/utils";
import type {RuntimeConfiguration} from "@/config/runtimeConfiguration";

const rc = inject('runtimeConfiguration') as RuntimeConfiguration;

const invoice = useInvoiceStore();

const route = useRoute();
const router = useRouter();

const invoiceData = computed(() => {
  return invoice.getCurrentInvoice;
});

onMounted(() => {
  invoice.getInvoice(route.params.uuid.toString());
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>Invoice {{ invoiceData.invoiceNumber }}</h1>
        <span class="status" v-if="invoiceData.invoiceStatus">
          <i class="ACTIVE" />
          {{ invoiceData.invoiceStatus }}</span
        >
      </div>
    </div>
    <div class="page-content">
      <div class="left-section">
        <div class="section-info">
          <div class="column-section">
            <div class="row-section">
              <div class="field">
                <span class="label">DATE CREATED</span>
                <span class="value">{{
                  formatDate(invoiceData.issueDateTime)
                }}</span>
              </div>
              <div class="field">
                <span class="label">BLOCKCHAIN REF</span>
                <a v-bind:href="rc.ipfsUrl + '/' + invoiceData.blockchainReference">
                    {{ invoiceData.blockchainReference }}
                </a>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">INVOICE VALUE</span>
                <span class="value">{{
                  invoiceData.amount?.amount + " " + invoiceData.amount.unit ||
                  0
                }}</span>
              </div>
              <div class="field">
                <span class="label">EXPECTED PAYMENT DATE</span>
                <span class="value">{{
                  formatDate(invoiceData.deadline)
                }}</span>
              </div>
            </div>
            <div class="row-section">
              <div class="field">
                <span class="label">DESCRIPTION</span>
                <span class="value">{{ invoiceData.freeTextDescription }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="right-section">
        <div class="section-info">
          <div class="column-section">
            <div class="row-section">
              <div class="field">
                <span class="label">SUPPLIER</span>
                <span class="value">{{
                  invoiceData.creditorData?.name || ""
                }}</span>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">CUSTOMER</span>
                <span class="value">{{
                  invoiceData.debtorData?.name || ""
                }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="section-info">
          <div class="field">
            <span class="label">ASSOCIATED MILESTONE</span>
            <span class="value">
              {{ invoiceData.milestone?.details?.name || "N/A"}} {{invoiceData.milestone?.uuid || "" }}
            </span>
          </div>
        </div>
        <div class="section-info">
          <div class="field">
            <span class="label">MARKETPLACE PRICE</span>
            <span class="value">
              {{ invoiceData.offerAmount?.amount || "N/A" }}
              {{ invoiceData.offerAmount?.unit || "" }}
            </span>
          </div>
          <div class="field">
            <span class="label">VIEW ON MARKETPLACE</span>
            <span class="value">{{
              invoiceData.toBeTraded ? "Yes" : "No"
            }}</span>
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
    background-color: #f7f7f7;
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
  }

  .right-section {
    flex-grow: 1;
  }

  .section-info {
    background: #ffffff;
    border: 1px solid #eeeeee;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    padding: 24px;
    margin-bottom: 24px;

    .field {
      color: var(--primary-color);
      display: flex;
      flex-direction: column;
      flex-grow: 1;
      max-width: 50%;

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
      background-color: #e9c300;
    }

    &.ACTIVE {
      background-color: #5dbb46;
    }
  }
}
</style>
