# --- Build stage ---
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# The frontend uses relative "/api" URLs, so no backend URL is needed at build time.
RUN npm run build

# --- Runtime stage ---
FROM nginx:1.27-alpine
# Template is rendered by the nginx entrypoint, substituting ${PORT} and ${BACKEND_URL}.
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
ENV PORT=80
ENV BACKEND_URL=http://localhost:8080
EXPOSE 80
