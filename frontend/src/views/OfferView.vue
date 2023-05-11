<script setup lang="ts">
import {computed, inject, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useOfferStore} from "@/stores/offer";
import {useUserStore} from "@/stores/user";
import {formatDate} from "@/utils/helpers/various";
import InputComponent from "@/components/InputComponent.vue";
import type {RuntimeConfiguration} from "@/config/runtimeConfiguration";

const rc = inject('runtimeConfiguration') as RuntimeConfiguration;

const offer = useOfferStore();
const user = useUserStore();

const route = useRoute();
const router = useRouter();

const offerData = computed(() => {
  return offer.getCurrentOffer;
});

onMounted(() => {
    offer.getOfferById(route.params.uuid.toString());
});

const dialogOpened = ref(false);
const dialogPriceOpened = ref(false);
const newOfferAmount = ref(0);

function toggleModal() {
  dialogOpened.value = dialogOpened.value ? false : true;
}

function togglePriceModal() {
  newOfferAmount.value = 0;
  dialogPriceOpened.value = dialogPriceOpened.value ? false : true;
}

function buyOffer() {
  offer.buyOffer(route.params.uuid.toString());
}

function updatePrice() {
  dialogPriceOpened.value = false;
  offer.updateOfferPrice(route.params.uuid.toString(), newOfferAmount.value);
}
</script>
<template>
  <div class="main-content">
    <sl-dialog
      label="Edit Price"
      class="dialog-width buy-offer"
      style="--width: 686px"
      :open="dialogPriceOpened"
    >
      <div class="milestones-price">
        <InputComponent label="New Price" v-model="newOfferAmount" />
      </div>
      <sl-button slot="footer" type="primary" @click="updatePrice()"
        >Update
      </sl-button>
    </sl-dialog>
    <sl-dialog
      label="Buy Offer"
      class="dialog-width buy-offer"
      style="--width: 686px"
      :open="dialogOpened"
    >
      <div class="info-section">
        <div class="field">
          <span class="label">OFFER</span>
          <span class="value">{{ offerData.invoiceNumber }}</span>
        </div>
        <div class="field">
          <span class="label align-right">PRICE</span>
          <span class="value align-right"
            >{{ offerData.price?.amount }} {{ offerData.price?.unit }}</span
          >
        </div>
        <div class="field">
          <span class="label align-right">AMOUNT</span>
          <span class="value align-right"
            >{{ offerData.amount?.amount }} {{ offerData.amount?.unit }}</span
          >
        </div>
      </div>
      <sl-button slot="footer" type="primary" @click="buyOffer()"
        >Proceed
      </sl-button>
    </sl-dialog>
    <div class="page-header">
      <div class="left-section">
        <h1>Offer for Invoice {{ offerData.invoiceNumber }}</h1>
        <span class="status">
          <i class="ACTIVE" />
          {{ offerData.state }}</span
        >
      </div>
      <sl-button
        v-if="user.user.uuid !== offerData.supplierUUID && offerData.state === 'open'"
        variant="primary"
        @click="toggleModal()"
        >Buy
      </sl-button>
      <sl-button
        v-else-if="user.user.uuid === offerData.supplierUUID && offerData.state === 'open'"
        variant="primary"
        @click="togglePriceModal()"
        >Edit Price
      </sl-button>
    </div>
    <div class="page-content">
      <div class="section-info">
        <div class="column-section">
          <div class="row-section">
            <div class="field">
              <span class="label">INVOICE NAME</span>
              <span class="value">{{ offerData.invoiceNumber }}</span>
            </div>
            <div class="field">
              <span class="label">BLOCKCHAIN REF</span>
              <a v-bind:href="rc.ipfsUrl + '/' + offerData.blockchainRef">
                  {{ offerData.blockchainRef ? offerData.blockchainRef : "N/A" }}
              </a>
            </div>
          </div>
          <sl-divider></sl-divider>
          <div class="row-section">
            <div class="field">
              <span class="label">PRICE</span>
              <span class="value raise"
                >{{ offerData.price?.amount }} {{ offerData.price?.unit }}<br />
              </span>
            </div>
            <div class="field">
              <span class="label">AMOUNT</span>
              <span class="value"
                >{{ offerData.amount?.amount }}
                {{ offerData.amount?.unit }}</span
              >
            </div>
            <div class="field">
              <span class="label">DATE LISTED</span>
              <span class="value">{{ formatDate(offerData.dateListed) }}</span>
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
                <span class="label">DEBTOR</span>
                <span class="value">{{ offerData.debtorData?.name }}</span>
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
                <span class="label">CUSTOMER</span>
                <span class="value">{{ offerData.supplierData?.name }}</span>
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
    flex-grow: 1;
    justify-content: space-between;
    padding: 24px;
    margin-bottom: 24px;

    .link {
      margin-top: 26px;
      font-weight: 500;
      font-size: 12px;
      line-height: 14px;
      //text-decoration-line: underline;
      text-transform: uppercase;
      color: var(--primary-color);
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

        &.raise {
          color: var(--success);
        }
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
      background-color: var(--orange);
    }

    &.ACTIVE {
      background-color: var(--success);
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
