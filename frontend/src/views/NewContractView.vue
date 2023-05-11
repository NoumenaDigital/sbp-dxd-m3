<script setup lang="ts">
import StepsComponent from "@/components/StepsComponent.vue";
import InputComponent from "@/components/InputComponent.vue";
import { computed, ref } from "vue";
import { useContractStore } from "@/stores/contract";
import { useUserStore } from "@/stores/user";
import CurrencySelectComponent from "@/components/CurrencySelectComponent.vue";

const user = useUserStore();

const accountData = computed(() => {
  return user.userData;
});

const currentStep = ref<number>(1);
const submitButtonText = ref<string>("Save & Continue");
const contract = useContractStore();

const newContract = ref({
  goodsValue: {},
  creditorData: {
    name: accountData.value.name + " " + accountData.value.surname,
    iban: accountData.value.details?.iban,
    address: accountData.value.details?.address
  },
  customerDetails: {},
  milestones: [],
} as any);

function addMilestone() {
  newContract.value.milestones.push({
    amount: { amount: 0, unit: newContract.value.goodsValue.unit },
  });
}

function save() {
  if (currentStep.value === 3) {
    const { creditorData, milestones, ...requestData } = newContract.value;
    const contractReq = {
      ...requestData,
      ccy: newContract.value.goodsValue.unit,
      milestones: milestones.map((milestone: any) => {
        return {
          ...milestone,
          paymentPeriod: `P${milestone.paymentPeriod}D`,
          offerAmount: milestone.offerAmount
            ? {
                amount: milestone.offerAmount,
                unit: newContract.value.goodsValue.unit,
              }
            : null,
        };
      }),
      id: newContract.value.name,
      creationDate: newContract.value.creationDate + "T00:00Z",
      expectedDeliveryDate: newContract.value.expectedDeliveryDate + "T00:00Z",
    };
    contract.addContract(contractReq);
  }
  currentStep.value = currentStep.value + 1;
  if (currentStep.value === 3) {
    addMilestone();
    submitButtonText.value = "Save & Finish";
  }
}
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>Add New Contract</h1>
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
              <span class="title">Contract Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Contract Name"
                    v-model="newContract.name"
                  />
                  <InputComponent
                    label="Date Created"
                    type="date"
                    v-model="newContract.creationDate"
                  />
                </div>
              </div>
            </div>
          </div>
          <div class="section">
            <div class="description-info">
              <span class="title">Shipment Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Good Value"
                    v-model="newContract.goodsValue.amount"
                  />
                  <CurrencySelectComponent
                    v-model="newContract.goodsValue.unit"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Expected Delivery Date"
                    type="date"
                    v-model="newContract.expectedDeliveryDate"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentStep === 2" class="section-info">
          <div class="section">
            <div class="description-info">
              <span class="title">Your Details</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="row-section">
                  <InputComponent
                    label="Your Name"
                    v-model="newContract.creditorData.name"
                    :disabled="true"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Address"
                    v-model="newContract.creditorData.address"
                    :disabled="true"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Account Number"
                    v-model="newContract.creditorData.iban"
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
                    v-model="newContract.customerDetails.name"
                  />
                </div>
                <div class="row-section">
                  <InputComponent
                    label="Address"
                    v-model="newContract.customerDetails.address"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentStep === 3" class="section-info">
          <div class="section">
            <div class="description-info">
              <span class="title">Milestones</span>
            </div>
            <div class="details-info">
              <div class="column-section">
                <div class="milestones-container">
                  <div
                    v-for="(newMilestone, index) in newContract.milestones"
                    class="milestone-item"
                  >
                    <div class="timeline">
                      <span class="milestone-no">{{ index + 1 }}</span>
                      <sl-divider :vertical="true"></sl-divider>
                    </div>
                    <div class="container">
                      <div class="row-section">
                        <InputComponent
                          label="Milestone Name"
                          v-model="newMilestone.name"
                        />
                      </div>
                      <div class="row-section">
                        <InputComponent
                          label="Amount Due"
                          v-model="newMilestone.amount.amount"
                        />
                        <CurrencySelectComponent
                          v-model="newMilestone.amount.unit"
                          :disabled="true"
                        />
                        <InputComponent
                          label="Payment Terms (days)"
                          v-model="newMilestone.paymentPeriod"
                        />
                      </div>
                      <div class="row-section">
                        <InputComponent
                          label="Description"
                          v-model="newMilestone.description"
                        />
                      </div>
                      <div class="marketplace-section">
                        <label
                          >List this invoice on marketplace to be traded?</label
                        >
                        <sl-radio-group label="Select an option" name="a">
                          <sl-radio
                            value="0"
                            @click="() => (newMilestone.toBeTraded = 0)"
                            >No
                          </sl-radio>
                          <sl-radio
                            value="1"
                            @click="() => (newMilestone.toBeTraded = 1)"
                            >Yes
                          </sl-radio>
                        </sl-radio-group>
                        <div
                          v-if="newMilestone.toBeTraded === 1"
                          class="row-section milestones-price"
                        >
                          <InputComponent
                            label="Price"
                            v-model="newMilestone.offerAmount"
                          />
                          <CurrencySelectComponent
                            v-model="newMilestone.amount.unit"
                            :disabled="true"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="milestone-item">
                    <div class="timeline">
                      <span class="milestone-no" @click="addMilestone()"
                        >+</span
                      >
                    </div>
                    <div class="container">
                      <span class="milestone-add" @click="addMilestone()"
                        >Click to Add Milestone</span
                      >
                    </div>
                  </div>
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
}

.milestones-container {
  .milestone-item {
    display: flex;

    .timeline {
      padding-right: 24px;

      sl-divider {
        margin-left: 24px;
      }
    }

    .container {
      flex-grow: 1;
      padding-bottom: 80px;
      gap: 40px;
      display: flex;
      flex-direction: column;

      .marketplace-section {
        padding-top: 60px;
      }
    }
  }

  .milestone-no {
    display: block;
    height: 48px;
    width: 48px;
    background: var(--primary-color-dark);
    border-radius: 4px;
    font-weight: 700;
    font-size: 22px;
    line-height: 48px;
    color: var(--white);
    text-align: center;
  }

  .milestone-add {
    font-weight: 400;
    font-size: 16px;
    line-height: 48px;
    color: var(--primary-color-dark);
  }

  .milestones-price {
    margin-top: 16px;
    width: 50%;
    padding: 24px;
    background: var(--white);
    border: 1px solid var(--light-light-gray);
    border-radius: 4px;
  }
}
</style>
