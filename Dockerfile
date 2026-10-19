FROM tomcat:10.1-jdk21-temurin AS compilacion
USER root
RUN apt-get update && apt-get install -y --no-install-recommends ant && rm -rf /var/lib/apt/lists/*
WORKDIR /proyecto
COPY . .
RUN ant -Dtomcat.home=/usr/local/tomcat dist

FROM tomcat:10.1-jdk21-temurin
RUN sed -i 's/<Server port="8005"/<Server port="-1"/' "$CATALINA_HOME/conf/server.xml"
COPY --from=compilacion /proyecto/lib/mssql-jdbc-13.6.0.jre11.jar /usr/local/tomcat/lib/
COPY config/context-render.xml /usr/local/tomcat/conf/Catalina/localhost/ROOT.xml
COPY --from=compilacion /proyecto/dist/EsSaludLoginWeb.war /usr/local/tomcat/webapps/ROOT.war
ENV JAVA_OPTS="-Duser.timezone=America/Lima" ESSALUD_HTTPS=true
ENV ESSALUD_POOL=true CATALINA_OPTS="-Dorg.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource"
EXPOSE 8080
CMD ["catalina.sh", "run"]
