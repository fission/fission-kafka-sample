# Sample Project 2

This sample project is inspired from and developed using https://github.com/YugaByte/yb-iot-fleet-management. The original example is meant as a microservice based example and we will transform that into an example which uses Fission functions.

There are three components:

1) The first component generates data about a fleet of vehicles and their lattitude, longitude, speed, fuel level etc. This function is trigerred every N seconds representing the sensor sending the data. This function sends this data to the Kafka topic.

2) Second component retrieves data from Kafka topics, runs some transformations and persists into YugaByte DB. In the current version this is again a time trigger based function but in future versions could be a streaming based function.

3) The last component is about visualizing the data fom YugaByte DB.

