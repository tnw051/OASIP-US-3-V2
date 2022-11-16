import axios, { AxiosError, RawAxiosRequestHeaders } from "axios";
import { accessTokenKey } from "../api/auth";
import { refreshAccessToken } from "./auth";

const baseURL = import.meta.env.PROD ? import.meta.env.VITE_API_URL : "/api";
export function makeUrl(path: string): string {
  return baseURL + path;
}

export const dank = axios.create({
  baseURL,
});

dank.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(accessTokenKey);
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
);

let retryCount = 0;
const maxRetry = 10;
const retryDelayMs = 500;

dank.interceptors.response.use(
  (response) => {
    console.log(`[api] ${response.config.url}:`, response.data);
    return response;
  },
  async (error: AxiosError) => {
    if (retryCount >= maxRetry) {
      console.log(`[api] retried ${retryCount} times, giving up`);
      retryCount = 0;
      throw error;
    }

    const { config, response } = error;
    const newHeaders = JSON.parse(JSON.stringify(config.headers || {})) as RawAxiosRequestHeaders;

    if (response && response.status === 401) {
      await new Promise((resolve) => setTimeout(resolve, retryDelayMs));
      retryCount++;
      await refreshAccessToken();
      return await dank.request({ 
        baseURL: config.baseURL, 
        url: config.url,
        method: config.method,
        data: config.data,
        headers: newHeaders,
        params: config.params,
      });
    }
  },
);