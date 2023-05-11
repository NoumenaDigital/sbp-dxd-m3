import qs from "qs";
import {
  ContractService,
  InvoiceService,
  OfferService,
  OpenAPI,
  UserService,
} from "./client";
import type { AccessToken } from "./client";
import axios from "axios";
import { useUserStore } from "@/stores/user";

const UnauthorizedRoutes = ["/sbp/api/v1/invoices/verify"];

OpenAPI.TOKEN = async (options) => {
  if (UnauthorizedRoutes.includes(options.url)) {
    return "";
  }

  const userStore = useUserStore();

  // await refresh when the bearer token is invalid
  if (!userStore.validBearer) {
    console.log("invalid token, await refresh");
    await userStore.refresh();
  }

  const { access } = userStore.decoded;
  const validNextSeconds = access.exp - Math.round(Date.now() / 1000);

  // refresh lazy when the bearer token is about to expire
  if (validNextSeconds < 15) {
    await userStore.refresh();
  }

  return userStore.token.access;
};

export class AuthService {
  /**
   * @name login
   * authenticates a user and resolves an access token
   *
   * @param {String} username
   * @param {String} password
   * @returns {AccessToken}
   */
  public static async login(username: string, password: string) {
    try {
      const { data } = await axios.post<AccessToken>(
        "/sbp/auth/login",
        qs.stringify({
          username,
          password,
          grant_type: "password",
        }),
        {
          baseURL: OpenAPI.BASE,
        }
      );
      return data;
    } catch (failure) {
      return Promise.reject(failure);
    }
  }

  /**
   * @name refresh
   * @description
   * Returns a fresh bearer and refresh token by providing the current refresh token
   *
   * @requires http
   * @param {String} refresh_token
   * @returns {Promise<models.AccessToken>}
   */
  public static async refresh(refresh_token: string) {
    try {
      const { data } = await axios.post<AccessToken>(
        "/sbp/auth/refresh",
        qs.stringify({
          grant_type: "refresh_token",
          refresh_token,
        }),
        {
          baseURL: OpenAPI.BASE,
        }
      );
      return data;
    } catch (failure) {
      return Promise.reject(failure);
    }
  }

  /**
   * @name logout
   * @description
   * Invalidates a current login
   *
   * @requires http
   * @param {String} refresh_token
   * @returns
   */
  public static async logout(refresh_token: string) {
    try {
      const response = await axios.post<AccessToken>(
        "/sbp/auth/logout",
        {
          refresh_token,
        },
        {
          baseURL: OpenAPI.BASE,
        }
      );
      return response;
    } catch (failure) {
      return Promise.reject(failure);
    }
  }
}

export * from "./models";
export const api = {
  authentication: AuthService,
  invoice: InvoiceService,
  contract: ContractService,
  user: UserService,
  offer: OfferService,
};
