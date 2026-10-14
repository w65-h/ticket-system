# syntax=docker/dockerfile:1
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN --mount=type=cache,target=/root/.npm \
    npm ci --registry=https://registry.npmmirror.com

COPY . .

RUN npm run build

FROM nginx:1.25-alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]