FROM node:18.8.0-alpine as builder

LABEL org.label-schema.name="sbpdxd-ui-portal" \
    org.label-schema.vendor="Noumena Digital" \
    org.label-schema.build-date="${BUILD_DATE}" \
    org.label-schema.vcs-ref="${GIT_REV}" \
    org.label-schema.version="${VERSION}" \
    org.opencontainers.image.source="https://github.com/NoumenaDigital/sbp-dxd/frontend"

ENV NGINX_HOST=localhost \
    NGINX_PORT=80

WORKDIR /app

COPY package.json .
COPY package-lock.json .

RUN npm ci

COPY docker ./docker
COPY public ./public
COPY src ./src

COPY env.d.ts index.html tsconfig.app.json tsconfig.config.json tsconfig.vitest.json tsconfig.json vite.config.ts ./

RUN npm run gen:api
RUN npm run build

FROM nginx:stable-alpine as release

COPY --from=builder /app/dist /usr/share/nginx/html
COPY --from=builder /app/docker/nginx/default.conf /etc/nginx/conf.d/default.conf

RUN apk add --update nodejs
RUN apk add --update npm

WORKDIR /usr/share/nginx/html

EXPOSE 80
CMD ["/bin/sh", "-c", "nginx -g \"daemon off;\""]
