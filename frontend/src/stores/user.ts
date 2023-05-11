import { api } from "@/data/api";
import { defineStore } from "pinia";
import {
    createCookie,
    eraseCookie,
    jwt,
    readCookie,
} from "@/utils";
import router from "../router";
import type {UserDetails} from "@/data/api/client";

export const ACCESS = "access_token";
export const REFRESH = "refresh_token";
export const PARTY = "party";
export const UUID = "uuid";
export type IExpire = { exp: number };
export type IParty = { party: string };

export type UserState = {
  token: { access: string; refresh: string };
  user: UserDetails;
  party: string;
};

export type UserGetters = {
  hasTokens(state: UserState): boolean;
  canRefresh(state: UserState): boolean;
  validBearer(state: UserState): boolean;
  profile(state: UserState): unknown;
  userData(state: UserState): UserDetails;
  authenticated(state: UserState): boolean;
  decoded(state: UserState): {
    access: IExpire;
    refresh: IExpire;
  };
  getParty(state: UserState): string;
};

export type UserActions = {
  login(username: string, password: string): Promise<void>;
  logout(): Promise<void>;
  refresh(): Promise<void>;
  loadUserProfile(): Promise<void>;
};

export const useUserStore = defineStore<
  "user",
  UserState,
  UserGetters,
  UserActions
>({
  id: "user",
  state: () => ({
    token: {
      access: readCookie(ACCESS),
      refresh: readCookie(REFRESH),
    },
    user: {} as UserDetails,
    party: readCookie(PARTY),
  }),
  getters: {
    decoded(state) {
      const access = jwt.decode<IExpire>(state.token.access);
      const refresh = jwt.decode<IExpire>(state.token.refresh);
      return {
        access: access.payload,
        refresh: refresh.payload,
      };
    },
    hasTokens(state) {
      return state.token.access !== "" && state.token.refresh !== "";
    },
    canRefresh(state) {
      if (!this.hasTokens) return false;
      const { exp } = jwt.decode(state.token.refresh).payload as IExpire;
      return exp * 1000 > Date.now();
    },
    validBearer(state) {
      if (!this.hasTokens) return false;
      const { exp } = jwt.decode(state.token.access).payload as IExpire;
      return exp > Date.now() / 1000;
    },
    userData(state) {
      return state.user;
    },
    profile(state) {
      return this.hasTokens
        ? (jwt.decode(state.token.access).payload as {
            exp: number;
          })
        : null;
    },
    authenticated() {
      return this.canRefresh;
    },
    getParty: (state) => {
      return state.party;
    },
  },
  actions: {
    async login(username: string, password: string) {
      try {
        const res = await api.authentication.login(username, password);
        const { party } = jwt.decode(res.access_token).payload as IParty;
        this.$patch({
          token: {
            access: createCookie(ACCESS, res.access_token),
            refresh: createCookie(REFRESH, res.refresh_token),
          },
          user: {},
          party: createCookie(PARTY, party[0]),
        });
        await this.loadUserProfile();
        await router.push({ name: "invoices" });
      } catch (error) {
        throw error;
      }
    },
    async logout() {
      try {
        await api.authentication.logout(this.token.access);
        this.$patch({
          token: {
            access: eraseCookie(ACCESS),
            refresh: eraseCookie(REFRESH),
          },
          user: {},
          party: eraseCookie(PARTY),
        });
      } catch (error) {
        this.$patch({
          token: {
            access: eraseCookie(ACCESS),
            refresh: eraseCookie(REFRESH),
          },
          user: {},
          party: eraseCookie(PARTY),
        });
        console.error('Error: "logout" action has failed', error);
        await router.push({ name: "login" });
      }
    },
    async refresh() {
      try {
        if (!this.canRefresh) {
          throw new Error("Refresh token has expired, cannot refresh.");
        } else {
          const res = await api.authentication.refresh(this.token.refresh);
          const { party } = jwt.decode(res.access_token).payload as IParty;
          this.$patch({
            token: {
              access: createCookie(ACCESS, res.access_token),
              refresh: createCookie(REFRESH, res.refresh_token),
            },
            user: {},
            party: createCookie(PARTY, party[0]),
          });
        }
      } catch (error) {
        console.error('[UserStore] Error: "refresh" action has failed', error);
        await router.push({ name: "Login" });
      }
    },
    async loadUserProfile() {
      const user = await api.user.getLoggedInUser();
      if (this.$state.user) Object.assign(this.$state.user, user);
      else this.$state.user = user;
    },
  },
});
