package io.fission.api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.Context;
import io.fission.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotApiVehicles implements Function  {
	
	private static Logger logger = Logger.getGlobal();
	final ObjectMapper mapper = new ObjectMapper();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis-single-redis.redis");

	public ResponseEntity call(RequestEntity req, Context context) {
		Jedis jedis = null;
		HashMap<String,String> vehicleCount = new HashMap<String,String>();
		String data = "";
		try {
			jedis = pool.getResource();
			vehicleCount.put("LARGE-TRUCK", jedis.hget("LARGE-TRUCK", "COUNT"));
			vehicleCount.put("SMALL-TRUCK", jedis.hget("SMALL-TRUCK", "COUNT"));
			vehicleCount.put("VAN", jedis.hget("VAN", "COUNT"));
			vehicleCount.put("CAR", jedis.hget("CAR", "COUNT"));
			vehicleCount.put("18-WHEELER", jedis.hget("18-WHEELER", "COUNT"));
			data = mapper.writeValueAsString(vehicleCount);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} finally {
			// You have to close jedis object. If you don't close then
			// it doesn't release back to pool and you can't get a new
			// resource from pool.
			if (jedis != null) {
				jedis.close();
			}
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		headers = IotApiFuel.AddCorsHeaders(headers);
		return ResponseEntity.ok().headers(headers).body(data);
	}

}
