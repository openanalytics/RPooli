<pre>
  ___ ___          _ _ 
 | _ \ _ \___  ___| (_)
 |   /  _/ _ \/ _ \ | |
 |_|_\_| \___/\___/_|_|

</pre>

# RPooli
_A pool of R nodes, exposed over RMI and managed over a RESTful API._


## Build

    mvn clean package

To produce a war that can be deployed on application servers, run:

    mvn -P-javax-dependencies clean package


## Integration test

The test requires the R packages RSBXml and RSBJson.

    rm /tmp/*.properties
    mvn -Pit clean verify


## Site generation

To generate the site for RPooli, run:

    mvn -f webapp/ site


## Start

    cd webapp
    mvn jetty:run-war

This starts servers on:

- web: http://localhost:8889/rpooli
- R pool: rmi://127.0.0.1/rpooli-pool


#### Copyright (c) Copyright of Open Analytics NV, 2014-2023

Licensed under the [Apache License 2.0](https://opensource.org/licenses/Apache-2.0)
