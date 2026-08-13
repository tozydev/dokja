FROM bellsoft/liberica-openjdk-debian:25 AS builder

WORKDIR /builder

COPY . .

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :api:bootJar --no-daemon --stacktrace \
    && cp api/build/libs/api-*.jar app.jar

RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM bellsoft/liberica-runtime-container:jre-25-musl

RUN addgroup -S dokja && adduser -S dokja -G dokja
USER dokja:dokja

WORKDIR /app
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
