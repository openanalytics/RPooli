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

## Integration test

    mvn clean verify

## Start

    cd webapp
    mvn jetty:run-war

This starts servers on:

- web: http://localhost:8889/rpooli
- R pool: rmi://127.0.0.1/rpooli-pool


## Copyright (c) Copyright of OpenAnalytics BVBA, 2014-2015

Licensed under the [GNU AFFERO GENERAL PUBLIC LICENSE, Version 3 (AGPL-3.0)](http://opensource.org/licenses/AGPL-3.0)
