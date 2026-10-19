FROM tomcat:10.1-jdk21-temurin AS compilacion
USER root
RUN apt-get update && apt-get install -y --no-install-recommends ant && rm -rf /var/lib/apt/lists/*
WORKDIR /proyecto
COPY . .
RUN ant -Dtomcat.home=/usr/local/tomcat dist

FROM tomcat:10.1-jdk21-temurin
COPY --from=compilacion /proyecto/dist/EsSaludLoginWeb.war /usr/local/tomcat/webapps/ROOT.war
ENV JAVA_OPTS="-Duser.timezone=America/Lima" ESSALUD_HTTPS=true
EXPOSE 8080
CMD ["catalina.sh", "run"]
