<script setup lang="ts">
import AvatarIcon from "@/components/icons/AvatarIcon.vue";
import {computed, onMounted} from "vue";
import {useUserStore} from "@/stores/user";

const user = useUserStore();

const accountData = computed(() => {
  return user.userData;
});

onMounted(() => {
  user.loadUserProfile();
});
</script>
<template>
  <div class="top-bar">
    <div class="user-info" @click="$router.push('/account')">
      <AvatarIcon/>
      <span>{{ accountData.details?.name || "" }}</span>
    </div>
  </div>
</template>
<style scoped lang="scss">
.top-bar {
  display: flex;
  flex-grow: 0;
  flex-shrink: 0;
  align-items: center;
  height: 74px;
  border-bottom: solid 1px rgba(21, 70, 129, 0.2);

  .user-info {
    display: flex;
    align-items: center;
    border-left: solid 1px rgba(21, 70, 129, 0.2);
    height: 74px;
    cursor: pointer;
    margin-left: auto;

    .avatar-icon {
      margin: 0 15px;
    }

    span {
      margin-right: 35px;
      font-weight: 400;
      font-size: 18px;
      line-height: 21px;
      color: var(--primary-color);
    }
  }
}
</style>
