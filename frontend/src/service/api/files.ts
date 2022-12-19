import { makeUrl } from "./client";

export function getDownloadUrl(bucketId: string, filename: string) {
  return makeUrl(`/events/files/${bucketId}/${filename}`);
}