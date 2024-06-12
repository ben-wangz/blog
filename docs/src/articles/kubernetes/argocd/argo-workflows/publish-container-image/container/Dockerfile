ARG BASE_IMAGE=alpine:3.20
FROM $BASE_IMAGE
RUN apk add --no-cache nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY random.string.txt /usr/share/nginx/html/random.string.txt
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]