# Fission Kafka - IOT Demo

This sample project is inspired from https://github.com/YugaByte/yb-iot-fleet-management. The original example is meant as a microservice based example and we will transform that into an example which uses Fission functions.

## Functions

1) IOT Data Producer: The first function generates data about a fleet of vehicles and their lattitude, longitude, speed, fuel level etc. This function is trigerred every N seconds resembling a sensor sending the data. This function sends this data to the a Kafka topic.

2) IOT Data Consumer: Second function retrieves data from Kafka topics, runs some transformations and persists into Redis. This function is trigerred for every message in the Kafka topic.

There are 4 more functions which read the data stored in Redis and expose them at REST endpoints. 

The Kafka consumer & 4 rest endpoint functions are all in same Spring project and form a single jar archive. The entrypoint for each function is different.

3) IOT Web: The last queries the data from rest endpoints and renders using chart.js, HTML & jQuery. This dashboard page itself is a function rendered using python flask & HTML.

## Architecture

![Architecture of the Fission Kafka - IOT Demo](/static_assets/architecture-diagram.png)

## Result

The resulting dashboard shows various trends. It also provides a way to invoke the IOT Data producer function which sends data to Kafka

![Dashboard: Fission Kafka - IOT Demo](/static_assets/iot-demo-screen.png)

## Try it out

### Quick Start (TBD)

If you want to quickly setup all the components and try out - run `deploy.sh` - the prerequisite is that Kubernetes cluster is setup and Fission is installed. 

### Detailed Guide

| Sr. No.        | Step details        | Link  |
|:-------------| :-------------: |:-----:|
|1| Setup Kafka & Redis | [Link](/00_setup/README.md)|
|2| Deploy environments & functions|[Link](/static_assets/deploy_functions.md)|
|3| Open dashoard & test|[Link](/static_assets/test.md)|