import { api } from "@/data/api";
import { defineStore } from "pinia";
import type {
  ContractRequest,
  ContractResponse,
  ExpandedMilestoneResponse,
} from "@/data/api/client";
import router from "@/router";
import { formatDate } from "@/utils";

export const useContractStore = defineStore({
  id: "contract",
  state: () => ({
    contracts: [] as Array<ContractResponse>,
    contractCurrent: {} as ContractResponse,
    milestoneCurrent: {} as ExpandedMilestoneResponse,
  }),
  getters: {
    getContracts: (state) =>
      state.contracts.map((contract: ContractResponse) => {
        return {
          uuid: contract.uuid,
          name: contract.name,
          value:
            contract.goodsValue?.amount?.toLocaleString("en-US", {
              style: "decimal",
              maximumFractionDigits: 2,
              minimumFractionDigits: 2,
            }) +
              " " +
              contract.goodsValue?.unit || 0,
          customer: contract.customerDetails?.name || "",
          dateCreated: formatDate(contract.creationDate),
          completionPercentage: contract.completionPercentage,
        };
      }),
    getCurrentContract: (state) => {
      return {
        ...state.contractCurrent,
        goodsQuantity: state.contractCurrent.goodsQuantity?.toLocaleString(),
        shipmentWeightKg:
          state.contractCurrent.shipmentWeightKg?.toLocaleString(),
        goodsValue: {
          amount: state.contractCurrent.goodsValue?.amount?.toLocaleString(
            "en-US",
            {
              style: "decimal",
              maximumFractionDigits: 2,
              minimumFractionDigits: 2,
            }
          ),
          unit: state.contractCurrent.goodsValue?.unit,
        },
        milestones: state.contractCurrent.milestones?.map((milestone: any) => {
          return {
            ...milestone,
            details: {
              ...milestone.details,
              amount: {
                amount:
                  milestone.details.amount?.amount.toLocaleString("en-US", {
                    style: "decimal",
                    maximumFractionDigits: 2,
                    minimumFractionDigits: 2,
                  }) || 0,
                unit: milestone.details.amount?.unit,
              },
            },
          };
        }),
      };
    },
    getCurrentMilestone: (state) => {
      return {
        ...state.milestoneCurrent,
        milestoneDetails: {
          ...state.milestoneCurrent.milestoneDetails,
          offerAmount: {
            amount:
              state.milestoneCurrent.milestoneDetails?.offerAmount?.amount?.toLocaleString(
                "en-US",
                {
                  style: "decimal",
                  maximumFractionDigits: 2,
                  minimumFractionDigits: 2,
                }
              ) || 0,
            unit: state.milestoneCurrent.milestoneDetails?.offerAmount?.unit,
          },
          amount: {
            amount:
              state.milestoneCurrent.milestoneDetails?.amount?.amount?.toLocaleString(
                "en-US",
                {
                  style: "decimal",
                  maximumFractionDigits: 2,
                  minimumFractionDigits: 2,
                }
              ) || 0,
            unit: state.milestoneCurrent.milestoneDetails?.amount?.unit,
          },
        },
      };
    },
  },
  actions: {
    async loadContracts() {
      try {
        const { contracts } = await api.contract.getContracts();
        this.contracts = contracts || [];
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getContract(uuid: string) {
      try {
        this.contractCurrent = await api.contract.getContract(uuid);
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getContractMilestone(contractUuid: string, milestoneUuid: string) {
      try {
        this.milestoneCurrent = await api.contract.getMilestone(
          contractUuid,
          milestoneUuid
        );
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async completeMilestone(contractUuid: string, milestoneUuid: string) {
      await api.contract.closeMilestone(contractUuid, milestoneUuid);
      await this.getContract(contractUuid);
    },
    async signContract(uuid: string) {
      await api.contract.signContract(uuid);
      await this.getContract(uuid);
    },
    async addContract(requestBody: ContractRequest) {
      const contractNew = await api.contract.createContract(requestBody);
      await router.push(`/contract/${contractNew.uuid}`);
    },
    async updateOfferPriceMilestone(
      contractUuid: string,
      milestoneUuid: string,
      amount: number,
      unit: string
    ) {
      await api.contract.updateOfferPriceMilestone(
        contractUuid,
        milestoneUuid,
        { offerPrice: { amount: amount, unit: unit } }
      );
      await this.getContractMilestone(contractUuid, milestoneUuid);
    },
  },
});
