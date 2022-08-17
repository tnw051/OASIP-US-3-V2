FROM node:lts-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN [ "npm", "install" ]

COPY . .
ARG VITE_API_URL
ARG BASE_URL
RUN [ "npm", "run", "build", "--", "--base", "$BASE_URL"]



FROM nginx:stable-alpine AS nginx

COPY --from=build /app/dist/ /usr/share/nginx/html/

COPY nginx/nginx.conf /etc/nginx/nginx.conf