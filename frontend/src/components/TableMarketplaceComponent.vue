<script setup lang="ts">
import {formatDate} from "@/utils";

interface formattedOfferResponse {
  uuid: string | undefined;
  offer: string | undefined;
  price: string | number;
  price_unit: string | undefined;
  amount: string | number;
  amount_unit: string | undefined;
  supplier: string;
  debtor: string;
  paymentDeadline: string | undefined;
}

const props = defineProps<{
  header: Array<string>;
  data: Array<formattedOfferResponse>;
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
        <td key="offer" class="offer">
          <RouterLink :to="`/offer/${row['uuid']}`">{{
              row["offer"]
            }}
          </RouterLink>
        </td>
        <td key="price" class="price">
          <RouterLink :to="`/offer/${row['uuid']}`">
              <span>{{ row["price"] }} {{ row["price_unit"] }}</span
              ><br/>
            <!--              <span>{{ row["percent"] }} %</span>-->
          </RouterLink>
        </td>
        <td key="amount" class="amount">
          <RouterLink :to="`/offer/${row['uuid']}`"
          >{{ row["amount"] }} {{ row["amount_unit"] }}
          </RouterLink
          >
        </td>
        <td key="supplier" class="supplier">
          <RouterLink :to="`/offer/${row['uuid']}`">
              <span>{{ row["supplier"] }}</span
              ><br/>
            <!--              <span>{{ row["supplier_rang"] }}</span>-->
          </RouterLink>
        </td>
        <td key="debtor" class="debtor">
          <RouterLink :to="`/offer/${row['uuid']}`">
              <span>{{ row["debtor"] }}</span
              ><br/>
            <!--              <span>{{ row["debtor_rang"] }}</span>-->
          </RouterLink>
        </td>
        <td key="due-date" class="due-date">
          <RouterLink :to="`/offer/${row['uuid']}`">{{
              formatDate(row["paymentDeadline"]) || "--/--/--"
            }}
          </RouterLink>
        </td>
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

  &:nth-of-type(2),
  &:nth-of-type(3) {
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

  &:nth-of-type(2),
  &:nth-of-type(3) {
    text-align: right;
  }

  &.price > a {
    color: var(--success);
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
    --indicator-color: var(--primary-color);
    --track-color: var(--light-gray);
  }
}
</style>
