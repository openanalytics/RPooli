FROM openanalytics/r-base
ADD . /webapp
WORKDIR /webapp
RUN apt-get clean && apt-get update && apt-get install -y openjdk-8-jdk libxml2-dev maven
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV R_HOME /usr/lib/R
RUN R CMD javareconf
RUN Rscript ./install_r_packages.R
ENTRYPOINT ["mvn", "clean", "verify", "-Pit"]