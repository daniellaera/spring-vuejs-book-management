FROM node:24-slim AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npx ng build --configuration=production

FROM nginx:alpine

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
