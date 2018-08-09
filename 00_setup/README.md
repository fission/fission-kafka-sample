# Fission Kafka - IOT Demo

## Setup Kafka
For setting up Kafka on Kubernetes, we will use [kubernetes-kafka repo here](https://github.com/Yolean/kubernetes-kafka). 

Follow the instructions in [Getting started section](https://github.com/Yolean/kubernetes-kafka#getting-started) and install Kafka & Zookeeper after appropriate storage class is configured.

**OPTIONAL** 

In addition you can install components below:
- yahoo-kafka-manager : Provides a nice UI to look at replication & rebalance stats across topics

- pixy: Provides a REST/GRPC API wrapper to Kafka. You can list, publish & consume messages using REST or GRPC.

## Setup Redis

We will setup redis without persistence and as a single instance (i.e. no master & slave). You can customize the command as per your needs based on [instrcutions from here](https://github.com/helm/charts/tree/master/stable/redis#configuration). Also note that for simplicity - Redis has been setup without a password.

```
helm install --name redis-single --namespace redis \
  --set usePassword=false \
  --set cluster.enabled=false \
  --set master.persistence.enabled=false \
    stable/redis
    ```