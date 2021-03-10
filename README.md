<p align="center">
  <img src="https://raw.githubusercontent.com/rwth-acis/las2peer/master/img/logo/bitmap/las2peer-logo-128x128.png" />
</p>
<h1 align="center">las2peer-Readerbench-Project</h1>





Please follow the instructions of this ReadMe to setup your basic service development environment.  

## Preparations
### Dependencies
[ReaderbenchpyApi](https://github.com/Karlydiamond214/readerbenchpyapi)

### Java

las2peer uses **Java 14**.

### Build Dependencies

* Gradle


## Quick Setup of your Service Development Environment

1. Compile your service with `gradle clean jar`. This will also build the service jar.  
2. Generate documentation, run your JUnit tests and generate service and user agent with `gradle clean build` (If this did not run check that the policy files are working correctly).  

## Database

## RUN with DOCKER

1.Build: `docker build . -t las2peer-readerbench`
2.Run:

`docker run -e MYSQL_USER=myuser -e MYSQL_PASSWORD=mypasswd -p 9011:9011 las2peer-readerbench`
