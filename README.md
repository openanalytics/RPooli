<pre>
  ___ ___          _ _ 
 | _ \ _ \___  ___| (_)
 |   /  _/ _ \/ _ \ | |
 |_|_\_| \___/\___/_|_|

</pre>

# RPooli

## Build

    mvn clean package

## Integration test

    mvn clean verify

## Start

    mvn jetty:run-war

This starts servers on:

- web: http://localhost:8889/rpooli
- R pool: rmi://127.0.0.1/rpooli-pool
 