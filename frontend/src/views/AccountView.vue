<script setup lang="ts">
import {computed, inject} from "vue";
import {useUserStore} from "@/stores/user";
import type {RuntimeConfiguration} from "@/config/runtimeConfiguration";

const rc = inject('runtimeConfiguration') as RuntimeConfiguration;

const user = useUserStore();

const accountData = computed(() => {
  return user.userData;
});
</script>
<template>
  <div class="main-content">
    <div class="page-header">
      <div class="left-section">
        <h1>My Account</h1>
      </div>
    </div>
    <div class="page-content">
      <div class="section-info">
        <div class="section">
          <div class="description-info">
            <span class="title">Your Details</span>
          </div>
          <div class="details-info">
            <div class="column-section">
              <div class="row-section">
                <div class="field">
                  <label>Your Name:</label>
                  <span>{{ accountData.name }} {{ accountData.surname }}</span>
                </div>
              </div>
              <div class="row-section">
                <div class="field">
                  <label>Email Address:</label>
                  <span>{{ accountData.mail }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section">
          <div class="description-info">
            <span class="title">Business Details</span>
          </div>
          <div class="details-info">
            <div class="column-section">
              <div class="row-section">
                <div class="field">
                  <label>Address:</label>
                  <span>{{ accountData.details?.address }}</span>
                </div>
              </div>
              <div class="row-section">
                <div class="field">
                  <label>IBAN:</label>
                  <span>{{ accountData.details?.iban }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="section">
          <div class="description-info">
            <span class="title">SBP Account Details</span>
          </div>
          <div class="details-info">
            <div class="column-section">
              <div class="field">
                  <label>Casper Account Details:</label>
                  <span>
                      <a v-bind:href="rc.casperUrl + '/' + accountData.accountHash">
                      {{ accountData.accountHash }}
                      </a>
                  </span>
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
}

.section-info {
  padding: 24px;
  display: flex;
  flex-grow: 1;
  flex-direction: column;
  gap: 24px;
  background: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;
}

.section {
  sl-button {
    margin-top: 35px;
  }

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

    .description {
      font-weight: 400;
      font-size: 14px;
      line-height: 20px;
      color: var(--black);
    }
  }

  .details-info {
    flex-grow: 2;
    max-width: 65%;

    .column-section {
      gap: 40px;
    }

    .field {
      display: flex;
      flex-direction: column;
      flex-grow: 1;

      label {
        font-weight: 400;
        font-size: 14px;
        line-height: 16px;
        color: var(--primary-color);
        margin-bottom: 11px;
      }

      span {
        font-weight: 400;
        font-size: 14px;
        line-height: 19px;
        color: var(--black);
      }
    }
  }
}
</style>
