<script setup lang="ts">
import StepsComponent from "@/components/StepsComponent.vue";
import InputComponent from "@/components/InputComponent.vue";
import { computed, ref } from "vue";
import { useInvoiceStore } from "@/stores/invoice";
import { useUserStore } from "@/stores/user";
import CurrencySelectComponent from "@/components/CurrencySelectComponent.vue";

const user = useUserStore();

const accountData = computed(() => {
  return user.userData;
});

const currentStep = ref<number>(1);
const submitButtonText = ref<string>("Save & Continue");
const invoice = useInvoiceStore();

const newInvoice = ref({
  amount: {},
  creditorData: {
    name: accountData.value.details?.name,
    iban: accountData.value.details?.iban,
    address: accountData.value.details?.address
  },
  debtorData: {},
} as any);

function save() {
  if (currentStep.value === 3) {
    const invoiceReq = {
      ...newInvoice.value,
      issueDateTime: newInvoice.value.issueDateTime + "T00:00Z",
      deadline: newInvoice.value.deadline + "T00:00Z",
      offerAmount: newInvoice.value.offerAmount
        ? {
            amount: newInvoice.value.offerAmount,
            unit: newInvoice.value.amount?.unit,
          }
        : null,
    };
    invoice.addInvoice(invoiceReq);
  }
  currentStep.value = currentStep.value + 1;
  if (currentStep.value === 3) {
    submitButtonText.value = "Create Invoice";
  }
}
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>Add New Invoice</h1>
      </div>
      <div class="right-section">
        <StepsComponent :steps="3" :active-step="currentStep" />
      </div>
    </div>
    <div class="page-content">
      <div class="section-info">
        <div v-if="currentStep === 1" class="section-info">
          <div class="section">
            <div class="description-info">
              <span class="title">Invoice Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Invoice ID"
                    v-model="newInvoice.invoiceNumber"
                  />
                  <InputComponent
                    label="Date Issued"
                    type="date"
                    v-model="newInvoice.issueDateTime"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Invoice Value"
                    required = true
                    v-model="newInvoice.amount.amount"
                  />
                  <CurrencySelectComponent v-model="newInvoice.amount.unit" />
                  <InputComponent
                    label="Expected Payment Date"
                    type="date"
                    v-model="newInvoice.deadline"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Description"
                    v-model="newInvoice.freeTextDescription"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentStep === 2" class="section-info">
          <div class="section">
            <div class="description-info">
              <span class="title">Marketplace</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="marketplace-section">
                  <label>List this invoice on marketplace to be traded?</label>
                  <sl-radio-group label="Select an option" name="a">
                    <sl-radio
                      value="0"
                      @click="() => (newInvoice.toBeTraded = 0)"
                      >No
                    </sl-radio>
                    <sl-radio
                      value="1"
                      @click="() => (newInvoice.toBeTraded = 1)"
                      >Yes
                    </sl-radio>
                  </sl-radio-group>
                  <div
                    v-if="newInvoice.toBeTraded === 1"
                    class="row-section marketplace-price"
                  >
                    <InputComponent
                      label="Price"
                      v-model="newInvoice.offerAmount"
                    />
                    <CurrencySelectComponent
                      v-model="newInvoice.amount.unit"
                      :disabled="true"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentStep === 3" class="section-info">
          <div class="section">
            <div class="description-info">
              <span class="title">Your Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Your Name"
                    v-model="newInvoice.creditorData.name"
                    :disabled="true"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Address"
                    v-model="newInvoice.creditorData.address"
                    :disabled="true"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Account Number"
                    v-model="newInvoice.creditorData.iban"
                    :disabled="true"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="section">
            <div class="description-info">
              <span class="title">Customer Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Full Name"
                    v-model="newInvoice.debtorData.name"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Address"
                    v-model="newInvoice.debtorData.address"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <sl-button variant="primary" @click="save()"
          >{{ submitButtonText }}
        </sl-button>
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

.section-info {
  margin-top: 60px;
  display: flex;
  flex-grow: 1;
  flex-direction: column;
  gap: 80px;
}

.section {
  display: flex;

  .description-info {
    display: flex;
    flex-direction: column;
    width: 35%;

    .title {
      font-weight: 700;
      font-size: 18px;
      line-height: 21px;
      color: var(--primary-color);
    }
  }

  .details-info {
    flex-grow: 2;
    max-width: 65%;

    .column-section {
      gap: 40px;
    }

    .row-section {
      gap: 30px;
    }
  }
}

sl-button {
  margin-top: 16px;
  align-self: end;
}

.marketplace-section {
  font-weight: 400;
  font-size: 14px;
  line-height: 16px;
  color: var(--primary-color);
  display: flex;
  flex-direction: column;

  sl-radio-group::part(base) {
    display: flex;
    margin-top: 24px;
  }

  sl-radio::part(label) {
    font-weight: 400;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
    margin: auto 62px auto 16px;
  }

  sl-radio::part(control) {
    color: var(--primary-color-dark);
    background-color: transparent;
    border-color: var(--primary-color-dark);
    height: 24px;
    width: 24px;
  }

  sl-radio::part(checked-icon) {
  }

  .marketplace-price {
    margin-top: 16px;
    width: 50%;
    padding: 24px;
  }
}
</style>
