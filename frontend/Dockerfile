# --- Build stage ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Backend URL is baked into the static bundle at build time.
ARG VITE_API_URL=http://localhost:8080
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

# --- Runtime stage ---
FROM nginx:1.27-alpine
# Template is rendered by the nginx entrypoint, substituting ${PORT} at startup.
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
ENV PORT=80
EXPOSE 80
