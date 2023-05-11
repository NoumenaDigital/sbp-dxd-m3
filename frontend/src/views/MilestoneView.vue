<script setup lang="ts">
import { useRoute, useRouter } from "vue-router";
import { computed, onMounted, ref } from "vue";
import { formatDate } from "@/utils";
import { useContractStore } from "@/stores/contract";
import InputComponent from "@/components/InputComponent.vue";

const contract = useContractStore();

const route = useRoute();
const router = useRouter();

const milestoneData = computed(() => {
  return contract.getCurrentMilestone;
});

const dialogOpened = ref(false);
const newOfferAmount = ref(0);

function toggleModal() {
  dialogOpened.value = dialogOpened.value ? false : true;
  newOfferAmount.value = 0;
}

function updatePrice() {
  dialogOpened.value = false;
  contract.updateOfferPriceMilestone(
    route.params.uuid.toString(),
    route.params.milestoneuuid.toString(),
    newOfferAmount.value,
    milestoneData.value.milestoneDetails?.amount?.unit || ""
  );
}

onMounted(() => {
  contract.getContractMilestone(
    route.params.uuid.toString(),
    route.params.milestoneuuid.toString()
  );
});
</script>
<template>
  <div class="main-content">
    <sl-dialog
      label="Edit Price"
      class="dialog-width buy-offer"
      style="--width: 686px"
      :open="dialogOpened"
    >
      <div class="info-section">
        <div class="field">
          <span class="label align-right">CURRENT PRICE</span>
          <span class="value align-right"
            >{{ milestoneData.milestoneDetails?.amount?.amount || 0 }}
            {{ milestoneData.milestoneDetails?.amount?.unit }}</span
          >
        </div>
        <div class="field">
          <span class="label align-right">AMOUNT</span>
          <span class="value align-right">
            {{ milestoneData.milestoneDetails?.offerAmount?.amount || 0 }}
            {{ milestoneData.milestoneDetails?.offerAmount?.unit }}</span
          >
        </div>
      </div>
      <div class="milestones-price">
        <InputComponent label="New Price" v-model="newOfferAmount" />
      </div>

      <sl-divider></sl-divider>
      <sl-button slot="footer" type="primary" @click="updatePrice()"
        >Update
      </sl-button>
    </sl-dialog>
    <div class="page-header">
      <div class="left-section">
        <h1>{{ milestoneData.milestoneDetails?.name }}</h1>
      </div>
    </div>
    <div class="page-content">
      <div class="section-info">
        <div class="column-section">
          <div class="row-section">
            <div class="field">
              <span class="label">DATE CREATED</span>
              <span class="value">{{
                formatDate(milestoneData.milestoneDetails?.dateCreated) ||
                "--/--/--"
              }}</span>
            </div>
            <div class="field">
              <span class="label">INVOICE VALUE</span>
              <span class="value">{{
                milestoneData.milestoneDetails?.amount?.amount +
                  " " +
                  milestoneData.milestoneDetails?.amount?.unit || 0
              }}</span>
            </div>
            <div class="field">
              <span class="label">EXPECTED PAYMENT DATE</span>
              <span class="value">{{
                milestoneData.milestoneDetails?.expectedPaymentDate
                  ? formatDate(
                      milestoneData.milestoneDetails.expectedPaymentDate
                    )
                  : "--/--/--"
              }}</span>
            </div>
          </div>
          <sl-divider></sl-divider>
          <div class="row-section">
            <div class="field">
              <span class="label">DESCRIPTION</span>
              <span class="value">{{
                milestoneData.milestoneDetails?.description
              }}</span>
            </div>

            <div class="field">
              <span class="label">DATE COMPLETED</span>
              <span class="value">{{
                milestoneData.milestoneDetails?.dateCompleted
                  ? formatDate(milestoneData.milestoneDetails?.dateCompleted)
                  : "--/--/--"
              }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="page-content">
      <div class="left-section">
        <div class="section-info">
          <div class="column-section">
            <div class="row-section">
              <div class="field">
                <span class="label">SUPPLIER</span>
                <span class="value">{{
                  milestoneData.supplierDetails?.name || "N/A"
                }}</span>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">CUSTOMER</span>
                <span class="value">{{
                  milestoneData.customerDetails?.name || "N/A"
                }}</span>
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
                <span class="label">PRICE</span>
                <span class="value">{{
                  milestoneData.milestoneDetails?.offerAmount &&
                  milestoneData.milestoneDetails.offerAmount.amount !== 0
                    ? milestoneData.milestoneDetails?.offerAmount?.amount +
                      " " +
                      milestoneData.milestoneDetails?.offerAmount?.unit
                    : "N\/A"
                }}</span>
              </div>
            </div>
            <sl-divider></sl-divider>
            <div class="row-section">
              <div class="field">
                <span class="label">VIEW ON MARKETPLACE</span>
                <span class="value">{{
                  milestoneData.milestoneDetails?.toBeTraded ? "Yes" : "No"
                }}</span>
              </div>
            </div>
            <span
              v-if="
                milestoneData.milestoneDetails.toBeTraded &&
                milestoneData.milestoneDetails.status === 'created'
              "
              class="link"
              @click="toggleModal()"
              >EDIT PRICE</span
            >
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
    flex-grow: 1;
    max-width: 50%;
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
    flex-grow: 1;

    .link {
      margin-top: 26px;
      font-weight: 500;
      font-size: 12px;
      line-height: 14px;
      //text-decoration-line: underline;
      text-transform: uppercase;
      color: var(--primary-color);
      cursor: pointer;
    }

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

.milestones-price {
  margin-top: 16px;
  width: 330px;
  padding: 24px;
  background: var(--white);

  .input-component {
    text-align: left;
  }
}

sl-dialog::part(title) {
  padding-left: 115px;
  text-align: center;
  font-family: "Roboto";
  font-style: normal;
  font-weight: 700;
  font-size: 18px;
  line-height: 21px;
  color: var(--primary-color);
}

sl-dialog::part(footer) {
  text-align: center;

  sl-button {
    width: 209px;
    height: 48px;
    background: var(--primary-color-dark);
    border-radius: 4px;
  }
}

sl-dialog::part(body) {
  font-style: normal;
  font-weight: 400;
  font-size: 16px;
  line-height: 22px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  color: var(--primary-color);
}

.info-section {
  width: 458px;
  height: 84px;
  background-color: rgba(218, 222, 244, 0.5);
  justify-content: space-around;
  display: flex;

  .field {
    display: flex;
    flex-direction: column;
    justify-content: center;

    .label {
      font-size: 12px;
      line-height: 14px;
      color: var(--primary-color);

      &.align-right {
        text-align: right;
      }
    }

    .value {
      font-weight: 500;
      font-size: 16px;
      line-height: 19px;

      &.align-right {
        text-align: right;
      }
    }
  }
}

.buy-offer {
  sl-divider {
    width: 100%;
  }
}

.paragraph {
  padding: 24px 56px;
}
</style>
