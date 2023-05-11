import {api} from "@/data/api";
import {defineStore} from "pinia";
import type {OfferResponse} from "@/data/api/client";
import router from "@/router";

export const useOfferStore = defineStore({
  id: "offer",
  state: () => ({
    offers: [] as Array<OfferResponse>,
    offerCurrent: {} as OfferResponse,
  }),
  getters: {
    getOffers: (state) =>
      state.offers.map((offer: OfferResponse) => {
        return {
          uuid: offer.uuid,
          offer: offer.invoiceNumber,
          price:
            offer.price?.amount?.toLocaleString("en-US", {
              style: "decimal",
              maximumFractionDigits: 2,
              minimumFractionDigits: 2,
            }) || 0,
          price_unit: offer.price?.unit,
          amount:
            offer.amount?.amount?.toLocaleString("en-US", {
              style: "decimal",
              maximumFractionDigits: 2,
              minimumFractionDigits: 2,
            }) || 0,
          amount_unit: offer.amount?.unit,
          supplier: offer.supplierData?.name || "",
          debtor: offer.debtorData?.name || "",
          paymentDeadline: offer.paymentDeadline,
        };
      }),
    getCurrentOffer: (state) => {
      return {
        ...state.offerCurrent,
        supplierUUID: state.offerCurrent.supplierUUID,
        price: {
          amount: state.offerCurrent.price?.amount?.toLocaleString("en-US", {
            style: "decimal",
            maximumFractionDigits: 2,
            minimumFractionDigits: 2,
          }),
          unit: state.offerCurrent.price?.unit,
        },
        amount: {
          amount: state.offerCurrent.amount?.amount?.toLocaleString("en-US", {
            style: "decimal",
            maximumFractionDigits: 2,
            minimumFractionDigits: 2,
          }),
          unit: state.offerCurrent.amount?.unit,
        },
      };
    },
  },
  actions: {
    async loadMarketplaceOffers() {
      try {
        const { offers } = await api.offer.getMarketplaceOffers();
        this.offers = offers || [];
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getSupplierOffers() {
      try {
        const { offers } = await api.offer.getMyIssuedOffers();
        this.offers = offers || [];
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getBuyerOffers() {
      try {
        const { offers } = await api.offer.getMyBoughtOffers();
        this.offers = offers || [];
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getOfferById(uuid: string) {
      try {
        this.offerCurrent = await api.offer.getOfferById(uuid);
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async buyOffer(offerUuid: string) {
      const offer = await api.offer.buyOffer(offerUuid);
      await router.push(`/portfolio`);
    },
    async updateOfferPrice(offerUuid: string, amount: number) {
      const offer = await api.offer.updateOfferPrice(offerUuid, {
        offerPrice: { amount: amount, unit: "EUR" },
      });
      await this.getOfferById(offerUuid);
    },
  },
});
