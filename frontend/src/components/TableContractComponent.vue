<script setup lang="ts">
interface formattedContractResponse {
  uuid: string | undefined;
  name: string | undefined;
  value: string | number;
  customer: string;
  dateCreated: string;
  completionPercentage: number | undefined;
}

const props = defineProps<{
  header: Array<string>;
  data: Array<formattedContractResponse>;
}>();
</script>
<template>
  <div class="table-container">
    <table>
      <thead>
      <tr>
        <th v-for="(value, index) in header" :key="index" scope="col">
          {{ value }}
        </th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(row, index) in data" :key="index" scope="col">
        <template v-for="(cellValue, cellIndex) in row" :key="cellIndex">
          <td v-if="cellIndex !== 'uuid'" :class="cellIndex">
              <span
              ><RouterLink :to="row['uuid'] ? `/contract/${row['uuid']}` : '#'">
                  <span v-if="cellIndex === 'completionPercentage'">
                    {{ cellValue }} %
                    <sl-progress-bar :value="cellValue"></sl-progress-bar>
                  </span>
                  <span v-else>{{ cellValue }}</span>
                </RouterLink>
              </span>
          </td>
        </template>
        <td></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>
<style scoped lang="scss">
div {
  box-sizing: border-box;
}

.table-container {
  margin-top: 24px;
  width: 100%;
  background: var(--white);
  border: 1px solid var(--light-light-gray);
  border-radius: 4px;
}

table {
  width: 100%;
  border-spacing: 0;
  border-collapse: collapse;
}

th {
  text-align: left;
  height: 48px;
  font-style: normal;
  font-weight: 700;
  font-size: 14px;
  line-height: 16px;
  color: var(--primary-color);
  border-bottom: 1px solid var(--light-light-gray);
  padding: 0 48px 0 24px;

  &:first-of-type {
    padding-left: 24px;
  }

  &:last-of-type {
    padding-right: 24px;
  }

  // hardcoded align for value column
  &:nth-of-type(2) {
    text-align: right;
  }
}

tr > td {
  height: 67px;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 16px;
  color: var(--primary-color);
  padding: 0 48px 0 24px;

  &.value {
    text-align: right;
  }

  a {
    text-decoration-line: none;

    &:hover {
      text-decoration-line: underline;
    }
  }

  &:first-of-type {
    padding-left: 24px;
  }

  &:last-of-type {
    padding-right: 24px;
  }
}

tr:nth-child(even) {
  background: rgba(238, 238, 238, 0.5);
}

.completion {
  width: 130px;
  font-family: "Inter", sans-serif;
  font-weight: 600;
  font-size: 10px;
  line-height: 12px;
  color: var(--primary-color-dark);

  sl-progress-bar {
    --height: 4px;
    indicator-color: var(--primary-color);
    track-color: var(--light-gray);
  }
}
</style>
