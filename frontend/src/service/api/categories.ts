import { CategoryResponse, EditCategoryRequest } from "../../gen-types";
import { Id } from "../../types";
import { dank } from "./client";

export async function getCategories(): Promise<CategoryResponse[]> {
  const response = await dank.get<CategoryResponse[]>("/categories");
  return response.data;
}

export async function getLecturerCategories(): Promise<CategoryResponse[]> {
  const response = await dank.get<CategoryResponse[]>("/categories/lecturer");
  return response.data;
}


export async function updateCategory(id: Id, editCategory: EditCategoryRequest): Promise<CategoryResponse> {
  const response = await dank.patch<CategoryResponse>(`/categories/${id}`, editCategory);
  return response.data;
}
