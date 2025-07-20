FROM node:20-alpine AS builder
ARG PRODUCTION=true
ARG HOST=http://localhost
ARG DOMAIN=localhost
ENV PRODUCTION=$PRODUCTION
ENV HOST=$HOST
ENV DOMAIN=$DOMAIN

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY . .

RUN echo "(function (window) {" > /app/src/assets/env.js && \
    echo "  window.__env = window.__env || {};" >> /app/src/assets/env.js && \
    echo "  window.__env.production = ${PRODUCTION};" >> /app/src/assets/env.js && \
    echo "  window.__env.apiUrl = '${HOST}/v1';" >> /app/src/assets/env.js && \
    echo "  window.__env.token_name = 'access_token';" >> /app/src/assets/env.js && \
    echo "  window.__env.domains = ['${DOMAIN}'];" >> /app/src/assets/env.js && \
    echo "  window.__env.disallowedRoutes = ['${HOST}/v1/auth/login', '${HOST}/v1/users/register'];" >> /app/src/assets/env.js && \
    echo "})(this);" >> /app/src/assets/env.js

RUN npm ci && npx ng build

FROM nginx:1.25-alpine
COPY --from=builder /app/dist/zekret-front/browser/ /usr/share/nginx/html
EXPOSE 80