FROM clojure
ENV PORT=8080
ENV JVM_OPTS="-Xmx250m"
COPY . /usr/src/app
WORKDIR /usr/src/app
EXPOSE ${PORT}
CMD lein run "${PORT}"
