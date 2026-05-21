# --- FASE 1: Build ---
FROM node:20-alpine AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Accetta VITE_API_URL come build argument
# Se vuoto, Axios userà path relativi (/api/...) che Nginx proxa verso il backend
ARG VITE_API_URL=
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

# --- FASE 2: Production con Nginx ---
FROM nginx:stable-alpine
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Configurazione Nginx:
# - Serve il frontend Vue su /
# - Proxa /api/* verso il backend container su porta 8080
# - /ws/** proxa WebSocket verso il backend
RUN printf 'server {\n\
    listen 80;\n\
    \n\
    # Frontend Vue\n\
    location / {\n\
        root /usr/share/nginx/html;\n\
        index index.html;\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
    \n\
    # Proxy API verso il backend Spring Boot\n\
    location /api {\n\
        proxy_pass http://backend:8080;\n\
        proxy_http_version 1.1;\n\
        proxy_set_header Upgrade $http_upgrade;\n\
        proxy_set_header Connection keep-alive;\n\
        proxy_set_header Host $host;\n\
        proxy_set_header X-Real-IP $remote_addr;\n\
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n\
        proxy_read_timeout 90s;\n\
    }\n\
    \n\
    # Proxy WebSocket verso il backend\n\
    location /ws {\n\
        proxy_pass http://backend:8080;\n\
        proxy_http_version 1.1;\n\
        proxy_set_header Upgrade $http_upgrade;\n\
        proxy_set_header Connection "upgrade";\n\
        proxy_set_header Host $host;\n\
    }\n\
}\n' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]