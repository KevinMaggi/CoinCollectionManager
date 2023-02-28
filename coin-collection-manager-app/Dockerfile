# JDK image
FROM eclipse-temurin:11

ARG jarToCopy

COPY /target/${jarToCopy} app/app.jar

# Add wait-for-it and make it executable
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh /
RUN chmod +x wait-for-it.sh

RUN apt-get update
RUN apt-get install -y libxtst6 libxrender1 libxi6

CMD ["sh", "-c", "./wait-for-it.sh postgres:5432 --timeout=30 --strict -- java -jar app/app.jar --postgres-url=$POSTGRES_URL --postgres-port=$POSTGRES_PORT --postgres-db=$POSTGRES_DB --postgres-user=$POSTGRES_USER --postgres-password=$POSTGRES_PASSWORD"]