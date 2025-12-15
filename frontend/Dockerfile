FROM node:20.18.0-slim AS build
WORKDIR /app

COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build --prod

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY ./config/nginx.conf /etc/nginx/conf.d/default.conf

COPY ./config/init.sh /usr/share/init.sh
RUN chmod +x /usr/share/init.sh

EXPOSE 80
ENTRYPOINT ["/usr/share/init.sh"]
CMD ["nginx", "-g", "daemon off;"]