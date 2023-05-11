<script setup lang="ts">
import SideBarComponent from "@/components/SideBarComponent.vue";
import TopBarComponent from "@/components/TopBarComponent.vue";
import { onMounted } from "vue";
import { useUserStore } from "@/stores/user";

const user = useUserStore();

onMounted(async () => {
  await user.loadUserProfile();
});
</script>
<template>
  <div class="user-management-view dashboard-page">
    <SideBarComponent />
    <div class="main-container">
      <TopBarComponent />
      <div class="main-content">
        <router-view></router-view>
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
        font-size: 16px;
        line-height: 19px;
        color: var(--black);
      }
    }
  }
}
</style>
