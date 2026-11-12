# syntax=docker/dockerfile:1.7
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci
COPY . .
# Firebase/Stripe keys are public client keys, passed at build time
ARG VITE_API_BASE_URL=/api
ARG VITE_FIREBASE_API_KEY=
ARG VITE_FIREBASE_AUTH_DOMAIN=
ARG VITE_FIREBASE_DATABASE_URL=
ARG VITE_FIREBASE_PROJECT_ID=
ARG VITE_FIREBASE_MESSAGING_SENDER_ID=
ARG VITE_FIREBASE_APP_ID=
ARG VITE_FIREBASE_VAPID_KEY=
RUN npm run build

FROM nginx:1.27-alpine
# API_UPSTREAM is substituted into the template when the container starts
ENV API_UPSTREAM=http://api-gateway:8080
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s CMD wget -qO- http://localhost/healthz || exit 1
