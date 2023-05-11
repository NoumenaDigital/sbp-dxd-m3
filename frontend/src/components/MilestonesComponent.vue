<script setup lang="ts">
import CheckIcon from "@/components/icons/CheckIcon.vue";
import {formatDate} from "@/utils/helpers/various";
import {useContractStore} from "@/stores/contract";

const contract = useContractStore();

const props = defineProps<{
  milestones: Array<any>;
  uuid: string;
  contractStatus?: string;
  contractSignDate?: string;
}>();

function markAsComplete(milestoneUuid: string) {
  contract.completeMilestone(props.uuid, milestoneUuid);
}

function showButton(status: string, index: number) {
  return (
      props.contractStatus === "creationFinalised" &&
      status === "created" &&
      (index > 0
          ? props.milestones[index - 1].details.status === "reached"
          : true)
  );
}
</script>
<template>
  <div class="steps-container">
    <div class="step-section">
      <template class="row-section">
        <div class="step-buffer">
          <span
              v-if="props.contractStatus === 'creationFinalised'"
              class="step finished"
          ><CheckIcon
          /></span>
          <span v-else class="step"></span>
          <sl-divider :vertical="true"></sl-divider>
        </div>
        <div class="column-section">
          <span>Contract signed & submitted</span>
          <span>{{
              props.contractSignDate
                  ? formatDate(props.contractSignDate)
                  : "--/--/--"
            }}</span>
        </div>
      </template>
    </div>
    <template
        v-for="(milestone, index) in props.milestones"
        class="step-section"
    >
      <template class="row-section">
        <div class="step-buffer">
          <span
              v-if="milestone.details.status === 'reached'"
              class="step finished"
          ><CheckIcon
          /></span>
          <span v-else class="step"></span>
          <sl-divider
              v-if="index + 1 !== milestones.length"
              :vertical="true"
          ></sl-divider>
        </div>
        <div class="column-section">
          <sl-tooltip
              :content="milestone.details.description"
              placement="right-center"
          >
            <span
                @click="
                $router.push(
                  `/contract/${props.uuid}/milestone/${milestone.uuid}`
                )
              "
            >Milestone {{ index + 1 }}: {{ milestone.details.name }}</span
            >
            <span
                @click="
                $router.push(
                  `/contract/${props.uuid}/milestone/${milestone.uuid}`
                )
              "
            >Amount due:
              <strong
              >{{ milestone.details.amount.amount }}
                {{ milestone.details.amount.unit }}</strong
              ></span
            >
            <span
                v-if="milestone.details.status === 'reached'"
                @click="
                $router.push(
                  `/contract/${props.uuid}/milestone/${milestone.uuid}`
                )
              "
            >Completed:
              <strong>{{
                  formatDate(milestone.details.dateCompleted)
                }}</strong></span
            >
          </sl-tooltip>
          <sl-button
              v-if="showButton(milestone.details.status, index)"
              variant="primary"
              @click="markAsComplete(milestone.uuid)"
          >Mark as Complete
          </sl-button
          >
        </div>
      </template>
    </template>
  </div>
</template>
<style scoped lang="scss">
.steps-container {
  display: flex;
  flex-direction: column;

  .step-buffer {
    display: flex;
    flex-direction: column;

    sl-divider {
      min-height: 60px;
      margin: 4px 5px 4px 12px;
    }
  }

  .row-section {
    font-weight: 400;
    font-size: 14px;
    line-height: 16px;
    color: var(--primary-color);
    //text-decoration: underline;
  }

  .column-section {
    display: flex;
    flex-direction: column;
    padding-left: 16px;

    span {
      cursor: pointer;
    }
  }

  .step {
    width: 24px;
    height: 24px;
    border-radius: 12px;
    flex-shrink: 0;
    background-color: var(--light-gray);
    display: flex;
    align-items: center;
    justify-content: center;

    &.finished,
    &.active {
      background-color: var(--success);
    }
  }

  sl-button {
    margin: 22px 0;
  }
}
</style>
