# ============================================================
# KAWA RETAILER PORTAL - Build React / Vite
# ============================================================

FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_FIREBASE_API_KEY
ARG VITE_FIREBASE_AUTH_DOMAIN
ARG VITE_FIREBASE_PROJECT_ID
ARG VITE_FIREBASE_APP_ID
ARG VITE_RETAILER_API_BASE_URL=
ARG VITE_BASE_PATH=/retailer-portal/

# Firebase web configuration is public, but must be supplied for a usable image.
RUN test -n "$VITE_FIREBASE_API_KEY" \
    && test -n "$VITE_FIREBASE_AUTH_DOMAIN" \
    && test -n "$VITE_FIREBASE_PROJECT_ID" \
    && test -n "$VITE_FIREBASE_APP_ID"

ENV VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY
ENV VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN
ENV VITE_FIREBASE_PROJECT_ID=$VITE_FIREBASE_PROJECT_ID
ENV VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID
ENV VITE_RETAILER_API_BASE_URL=$VITE_RETAILER_API_BASE_URL
ENV VITE_BASE_PATH=$VITE_BASE_PATH

RUN npm run build


# ============================================================
# Runtime Nginx
# ============================================================

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
