# --- FASE 1: Build ---
FROM node:20-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# --- FASE 2: Production ---
FROM nginx:stable-alpine
# Copiamo i file compilati dalla fase precedente nella cartella di Nginx
COPY --from=build-stage /app/dist /usr/share/nginx/html
RUN echo 'server { listen 80; location / { root /usr/share/nginx/html; index index.html; try_files $uri $uri/ /index.html; } }' > /etc/nginx/conf.d/default.conf
# Copiamo una configurazione personalizzata di Nginx (opzionale, ma consigliata per Vue Router)
# Se non la hai, Nginx userà quella di default.
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]