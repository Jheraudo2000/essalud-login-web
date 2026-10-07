FROM eclipse-temurin:17-jdk-jammy AS build
RUN apt-get update && apt-get install -y --no-install-recommends ant && rm -rf /var/lib/apt/lists/*
WORKDIR /proyecto
COPY build.xml ./
COPY nbproject/ ./nbproject/
COPY src/ ./src/
COPY web/ ./web/
RUN ant compile

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /proyecto/build/classes/ ./classes/
ENV PORT=10000
EXPOSE 10000
USER 10001:10001
CMD ["java", "-XX:MaxRAMPercentage=70.0", "-cp", "classes", "essalud.Aplicacion"]
