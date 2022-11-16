import { UserResponse, Role, CreateUserRequest, EditUserRequest } from "../../gen-types";
import { Id } from "../../types";
import { dank } from "./client";

interface GetUsersOptions {
  onUnauthorized?: () => void;
}
export async function getUsers(options: GetUsersOptions = {
}): Promise<UserResponse[]> {
  try {
    const response = await dank.get<UserResponse[]>("/users");
    return response.data;
  } catch (error) {
    if (error.response.status === 401) {
      options.onUnauthorized?.();
    }
    throw error;
  }
}

export async function getRoles(): Promise<Role[]> {
  const response = await dank.get<Role[]>("/users/roles");
  return response.data;
}

export async function createUser(newUser: CreateUserRequest): Promise<UserResponse> {
  const response = await dank.post<UserResponse>("/users", newUser);
  return response.data;
}

export async function deleteUser(id: Id): Promise<boolean> {
  const response = await dank.delete(`/users/${id}`);
  return response.status === 204;
}

export async function updateUser(id: Id, changes: EditUserRequest): Promise<UserResponse> {
  const response = await dank.patch<UserResponse>(`/users/${id}`, changes);
  return response.data;
}
