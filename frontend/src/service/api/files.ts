import { dank, makeUrl } from "./client";

export async function getFilenameByBucketUuid(uuid: string): Promise<string | null> {
  const response = await dank.get(`/events/files/${uuid}?noContent=true`);
  return response.headers["content-disposition"]?.split("filename=")[1] ?? null;
}

export function getBucketURL(uuid: string) {
  return makeUrl(`/events/files/${uuid}`);
}