FROM openjdk:17-oracle
ENV JAVA_PARAMETERS="-Dorg.apache.jasper.compiler.disablejsr199=false -Xrs -Xmx1024m"
COPY backend/puretherapie-crm-1.0.0.jar message-server-1.0.0.jar
COPY backend/application.properties application.properties
ENTRYPOINT ["java", "-Dconfig.location=./application.properties","-jar", "$JAVA_PARAMETERS","./puretherapie-crm-*.jar"]