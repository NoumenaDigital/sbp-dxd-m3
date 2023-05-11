import { api } from "@/data/api";
import { defineStore } from "pinia";
import type { InvoiceRequest, InvoiceResponse } from "@/data/api/client";
import { formatDate, getBase64 } from "@/utils";
import router from "@/router";

export const useInvoiceStore = defineStore({
  id: "invoice",
  state: () => ({
    invoices: [] as Array<InvoiceResponse>,
    invoiceCurrent: {} as InvoiceResponse,
  }),
  getters: {
    getInvoices: (state) =>
      state.invoices.map((invoice: InvoiceResponse) => {
        return {
          uuid: invoice.uuid,
          invoiceId: invoice.invoiceNumber,
          value:
            invoice.amount?.amount?.toLocaleString("en-US", {
              style: "decimal",
              maximumFractionDigits: 2,
              minimumFractionDigits: 2,
            }) +
              " " +
              invoice.amount?.unit || 0,
          customer: invoice.debtorData?.name || "",
          dateCreated: formatDate(invoice.issueDateTime),
          paymentExpected: formatDate(invoice.deadline),
          status: invoice.invoiceStatus,
        };
      }),
    getCurrentInvoice: (state) => {
      return {
        ...state.invoiceCurrent,
        amount: {
          amount: state.invoiceCurrent.amount?.amount?.toLocaleString("en-US", {
            style: "decimal",
            maximumFractionDigits: 2,
            minimumFractionDigits: 2,
          }),
          unit: state.invoiceCurrent.amount?.unit,
        },
      };
    },
  },
  actions: {
    async verifyInvoice(file: any) {
      const fileEncoded = await getBase64(file);
      try {
        return await api.invoice.verifyInvoiceInternally({
          type: "xml",
          base64EncodedFile: fileEncoded.split(",")[1],
        });
      } catch (error) {
        return error;
      }
    },
    async loadInvoices() {
      try {
        const { invoices } = await api.invoice.getInvoices();
        this.invoices = invoices || [];
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async getInvoice(uuid: string) {
      try {
        this.invoiceCurrent = await api.invoice.getInvoice(uuid);
      } catch (err) {
        console.error(err, "error");
        return [];
      }
    },
    async addInvoice(requestBody: InvoiceRequest) {
      const invoiceNew = await api.invoice.createInvoice(requestBody);
      await router.push(`/invoice/${invoiceNew.uuid}`);
    },
  },
});
