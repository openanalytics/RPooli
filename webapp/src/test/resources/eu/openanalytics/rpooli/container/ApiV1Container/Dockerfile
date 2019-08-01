FROM openanalytics/r-base
RUN apt-get clean && apt-get update && apt-get install -y openjdk-8-jdk libxml2-dev
COPY ./scripts /opt/rpooli/scripts
RUN wget https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-distribution/9.4.19.v20190610/jetty-distribution-9.4.19.v20190610.tar.gz -O /opt/jetty.tar.gz
WORKDIR /opt
RUN tar -xvzf jetty.tar.gz
RUN mv $(find . -type d -name "jetty-*") jetty
COPY ./rpooli.war /opt/jetty/webapps/rpooli.war
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV R_HOME /usr/lib/R
RUN R CMD javareconf
RUN Rscript /opt/rpooli/scripts/install_r_packages.R
WORKDIR /opt/jetty
ENTRYPOINT ["java", "-jar", "start.jar"]
