# Testing the demo

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
