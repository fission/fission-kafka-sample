package io.fission.kafka;

import java.lang.Thread;
import java.lang.Integer;
import java.util.HashMap;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotConsumer implements Function {

	private static Logger logger = Logger.getGlobal();

	JedisPool pool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_ADDR"));
	final ObjectMapper mapper = new ObjectMapper();

	public ResponseEntity call(RequestEntity req, Context context) {
		HashMap data = (HashMap) req.getBody();
		int sleepDelay = Integer.parseInt(System.getenv("CONSUMER_SLEEP_DELAY"));
		try {
			Thread.sleep(sleepDelay);
		} catch(Exception exception) {
			logger.info("Exception in thread sleep" + exception);
		}
		logger.info("Data=" + data.toString());
		IoTData iotData = mapper.convertValue(data, IoTData.class);
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			// - Add RouteID <> FuelLevel, limited to 10K records
			jedis.lpush((iotData.getRouteId()+"-FUEL").toUpperCase(), Double.toString(iotData.getFuelLevel()));
			jedis.ltrim((iotData.getRouteId()+"-FUEL").toUpperCase(), 0, 10000);
			// - Add RouteID <> Speed, limited to 10K records
			jedis.lpush((iotData.getRouteId()+"-SPEED").toUpperCase(), Double.toString(iotData.getSpeed()));
			jedis.ltrim((iotData.getRouteId()+"-SPEED").toUpperCase(), 0, 10000);
			// - Add Increment Vehicle type by 1
			jedis.hincrBy((iotData.getVehicleType().replace(" ", "-")).toUpperCase(), "COUNT", 1);	
			jedis.hincrBy("RECORD_ACK_BY_CONSUMER", "COUNT", 1);
		
			// - Add Data of Speed across times.
			jedis.hset((iotData.getRouteId()+"-DATA").toUpperCase(), iotData.getTimestamp().toString(), Double.toString(iotData.getSpeed()));			
		} finally {
			// You have to close jedis object. If you don't close then
			// it doesn't release back to pool and you can't get a new
			// resource from pool.
			if (jedis != null) {
				jedis.close();
			}
		}

		return ResponseEntity.status(HttpStatus.OK).build();
	}
}