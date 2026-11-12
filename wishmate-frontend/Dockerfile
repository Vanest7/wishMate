FROM node:24-bookworm-slim AS build
WORKDIR /app
ENV CI=true HUSKY=0 NX_DAEMON=false

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY . .
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.wishmate.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/apps/wishmate/browser/ /usr/share/nginx/html/
