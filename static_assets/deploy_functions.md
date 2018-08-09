# Deploying Functions

- Before deploying functions, the artifacts needs to be built from source code. You can either use builder for that or build them locally. 

  - To build locally:

    ```
    $ mvn -f 01_iot_data_producer/pom.xml clean package
    $ mvn -f 02_iot_data_consumer/pom.xml clean package
    ```

- All the environment, package and function definitions are in specs directory. To create all of them run command:

```$ fission spec apply ```

- You can check details of deployed functions:

```$ fission fn list ```