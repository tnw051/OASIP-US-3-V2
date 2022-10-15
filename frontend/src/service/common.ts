export const accessTokenKey = "accessToken";

const baseUrl = import.meta.env.PROD ? import.meta.env.VITE_API_URL : "/api";

export function makeUrl(path: string) {
  return `${baseUrl}${path}`;
}