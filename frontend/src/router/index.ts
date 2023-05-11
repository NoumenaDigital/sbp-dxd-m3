import { createRouter, createWebHistory } from "vue-router";
import type { RouteLocationNormalized } from "vue-router";
import VerifyInvoiceView from "@/views/VerifyInvoiceView.vue";
import LoginView from "@/views/LoginView.vue";
import InvoicesView from "@/views/InvoicesListView.vue";
import NewInvoiceView from "@/views/NewInvoiceView.vue";
import InvoiceView from "@/views/InvoiceView.vue";
import AccountView from "@/views/AccountView.vue";
import { useUserStore } from "@/stores/user";
import HomeView from "@/views/HomeView.vue";
import MarketplaceView from "@/views/MarketplaceView.vue";
import OfferView from "@/views/OfferView.vue";
import PortfolioView from "@/views/PortfolioView.vue";
import ContractView from "@/views/ContractView.vue";
import MilestoneView from "@/views/MilestoneView.vue";
import ContractsView from "@/views/ContractsListView.vue";
import NewContractView from "@/views/NewContractView.vue";
import OffersView from "@/views/OffersView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/verify-invoice",
      name: "verify-invoice",
      component: VerifyInvoiceView,
      meta: { requiresAuth: false },
    },
    {
      path: "/login",
      name: "login",
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: "/",
      name: "home",
      component: HomeView,
      children: [
        { path: "", name: "dashboard", component: InvoicesView },
        {
          path: "invoices",
          name: "invoices",
          component: InvoicesView,
          meta: { requiresAuth: true },
        },
        {
          path: "new-invoice",
          name: "new-invoice",
          component: NewInvoiceView,
          meta: { requiresAuth: true },
        },
        {
          path: "invoice/:uuid",
          name: "invoice",
          component: InvoiceView,
          meta: { requiresAuth: true },
        },
        {
          path: "contracts",
          name: "contracts",
          component: ContractsView,
          meta: { requiresAuth: true },
        },
        {
          path: "contract/:uuid",
          name: "contract",
          component: ContractView,
          meta: { requiresAuth: true },
        },
        {
          path: "contract/:uuid/milestone/:milestoneuuid",
          name: "contract-milestone",
          component: MilestoneView,
          meta: { requiresAuth: true },
        },
        {
          path: "new-contract",
          name: "new-contract",
          component: NewContractView,
          meta: { requiresAuth: true },
        },
        {
          path: "account",
          name: "account",
          component: AccountView,
          meta: { requiresAuth: true },
        },
        {
          path: "marketplace",
          name: "marketplace",
          component: MarketplaceView,
          meta: { requiresAuth: true },
        },
        {
          path: "offers",
          name: "offers",
          component: OffersView,
          meta: { requiresAuth: true },
        },
        {
          path: "offer/:uuid",
          name: "offer",
          component: OfferView,
          meta: { requiresAuth: true },
        },
        {
          path: "portfolio",
          name: "portfolio",
          component: PortfolioView,
          meta: { requiresAuth: true },
        },
      ],
    },
  ],
});

export default router;

router.beforeEach((to: RouteLocationNormalized) => {
  const user = useUserStore();
  if (to.name === "login") {
    if (!user.hasTokens) return true;
    return user.logout().then(() => ({ name: "login" }));
  }

  const publicRoutes = ["verify-invoice"];
  if (publicRoutes.includes(to.name as string)) {
    return true;
  }

  if (!user.validBearer && !user.canRefresh) {
    return { name: "login" };
  }

  return true;
});
