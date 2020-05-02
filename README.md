Table of Contents
=================

   * [Table of Contents](#table-of-contents)
   * [Fission Kafka - IoT Demo](#fission-kafka---iot-demo)
      * [Architecture](#architecture)
         * [Functions](#functions)
      * [Result](#result)
      * [Setup](#setup)
         * [Prerequisite](#prerequisite)
         * [Setup Kafka](#setup-kafka)
         * [Setup Redis](#setup-redis)
         * [Configuration Check/Change](#configuration-checkchange)
         * [Build Functions](#build-functions)
         * [Deploy functions](#deploy-functions)
         * [Testing](#testing)

Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)


# Fission Kafka - IoT Demo

This sample project is inspired from [Yugabyte's IOT fleet management demo](https://github.com/YugaByte/yb-iot-fleet-management). The original example was built using  microservice, this demo will transform that into an example which uses Fission functions.

## Architecture

![Architecture of the Fission Kafka - IOT Demo](/static_assets/architecture-diagram.png)

### Functions

1. IOT Data Producer: This function generates data about a fleet of vehicles and their lattitude, longitude, speed, fuel level etc. This function can be trigerred every N seconds resembling a sensor sending the data. This function sends this data to the a Kafka topic: iotdata (Configured in environment variable of IOT data Producer function).

2. IOT Data Consumer: Second function retrieves data from Kafka topics, runs some transformations and persists into Redis. This function is trigerred for every message in the Kafka topic.

3. There are 4 more functions which read the data stored in Redis and expose them at REST endpoints: count, fuelavg, data & speedavg

4. The Kafka consumer & 4 rest endpoint functions are all in same Spring project and form a single jar archive. The entrypoint for each function is different.

5. IOT Web: The last queries the data from rest endpoints and renders using chart.js, HTML & jQuery. This dashboard page itself is a function rendered using python flask & HTML.


## Result

The resulting dashboard shows various trends. It also provides a way to invoke the IOT Data producer function which sends data to Kafka. Sample screen:

![Dashboard: Fission Kafka - IOT Demo](/static_assets/iot-demo-screen.png)

## Setup

### Prerequisite

- A Kubernetes cluster with latest version of Fission
- Fission installation should have Kafka MQT installed with address of Kafka brokers.
- Docker and Fission-CLI on local machine

### Setup Kafka

Easiest way to install Kafka is to start with Quickstart of [Strimzi Kafka Operator](https://strimzi.io/quickstarts/). 

```
$ kubectl create namespace kafka

$ kubectl apply -f 'https://strimzi.io/install/latest?namespace=kafka' -n kafka

$ kubectl apply -f https://strimzi.io/examples/latest/kafka/kafka-persistent-single.yaml -n kafka 

```

### Setup Redis

We will setup redis without persistence and as a single instance (i.e. no master & slave). You can customize the command as per your needs based on [instrcutions from here](https://github.com/helm/charts/tree/master/stable/redis#configuration). Also note that for simplicity - Redis has been setup without a password.

```

$ helm repo add bitnami https://charts.bitnami.com/bitnami

$ helm install redis-single --namespace redis \
  --set usePassword=false \
  --set cluster.enabled=false \
  --set master.persistence.enabled=false \
    bitnami/redis
```


### Configuration Check/Change

Some key configuration need to be checked for having proper values:

- In specs/env-java.yaml check KAFKA_ADDR is pointing to appropriate address of Kafka cluster created in earlier section

- Right now the address of Redis is hardcoded in source code in the `02_iot_data_consumer` folder at seven places. Ensure the address is correct redis URL and correct if not right.

```
JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis-single-master.redis");
```

- You will also need to update the Fission installation if not already to enable the Kafka MQT trigger with appropriate configuration of broker address etc.

```
## Kafka: enable and configure the details
kafka:
  enabled: true
  # note: below link is only for reference. 
  # Please use the brokers link for your kafka here. 
  brokers: 'my-cluster-kafka-0.my-cluster-kafka-brokers.kafka.svc:9092' # or your-bootstrap-server.kafka:9092/9093
  authentication:
```



### Build Functions

The two java functions need to be built locally from directories `01_iot_data_producer` and `02_iot_data_consumer`. Without installing JDK locally, we will build them inside a docker container with following command:

```
 $ docker run -it --rm --name my-men-project -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn clean install
```


### Deploy functions

- All the environment, package and function definitions are in specs directory. To create all of them run command:

```
$ fission spec apply 
```

- You can check details of deployed functions:

```
$ fission fn list 
```


### Testing

- To generate some sample data, you can run the Kafka producer (kp) function a few times or create a trigger which invokes the function every N minutes:

```
# Creating trigger to produce data every 5 minutes
$ fission tt create --name kptrigger --function kp --cron '@every 5m'
trigger 'kptrigger' created
Current Server Time: 	2018-08-09T10:58:38Z
Next 1 invocation: 	2018-08-09T11:03:38Z
$ fission tt list
NAME      CRON      FUNCTION_NAME
kptrigger @every 5m kp

# Calling the producer function directly.
$ fission fn test --name kp
```

- After the Kafka producer function has trigerred at least once, you can open the dashboard function in browser:

```http://$FISSION_ROUTER/iot/index.html```
