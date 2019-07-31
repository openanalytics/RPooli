FROM openanalytics/r-base
COPY ./scripts /opt/rpooli/scripts
COPY ./jetty /opt/jetty
COPY ./rpooli-1.1.0.war /opt/jetty/webapps/rpooli.war
RUN apt-get clean && apt-get update && apt-get install -y openjdk-8-jdk libxml2-dev
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV R_HOME /usr/lib/R
RUN R CMD javareconf
RUN Rscript /opt/rpooli/scripts/install_r_packages.R
WORKDIR /opt/jetty
ENTRYPOINT ["java", "-jar", "start.jar"]
