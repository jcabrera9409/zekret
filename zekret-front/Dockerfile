FROM node:20-alpine AS builder
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY . .
RUN npm ci && npx ng build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist/zekret-front/browser/ /usr/share/nginx/html