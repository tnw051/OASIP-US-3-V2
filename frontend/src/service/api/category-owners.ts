import { CategoryOwnerResponse } from "../../gen-types";
import { dank } from "./client";

export async function getCategoryOwners(): Promise<CategoryOwnerResponse[]> {
  const response = await dank.get<CategoryOwnerResponse[]>("/category-owners");
  return response.data;
}