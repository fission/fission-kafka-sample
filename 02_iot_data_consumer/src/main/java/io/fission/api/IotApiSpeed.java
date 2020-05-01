package io.fission.api;

import java.util.HashMap;
import java.util.List;
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

public class IotApiSpeed implements Function  {
	
	private static Logger logger = Logger.getGlobal();
	final ObjectMapper mapper = new ObjectMapper();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis-single-master.redis");


	public ResponseEntity call(RequestEntity req, Context context) {
		Jedis jedis = null;
		HashMap<String, Integer> averageFuel = new HashMap<String, Integer>();
		String data = "";
		try {
			jedis = pool.getResource();
			List<String> route82 =  jedis.lrange("ROUTE-82-SPEED", 0, 9999);
			List<String> route37 =  jedis.lrange("ROUTE-37-SPEED", 0, 9999);
			List<String> route43 =  jedis.lrange("ROUTE-43-SPEED", 0, 9999);
			
			averageFuel.put("ROUTE-82", IotApiFuel.calculateAverage(route82));
			averageFuel.put("ROUTE-37", IotApiFuel.calculateAverage(route37));
			averageFuel.put("ROUTE-43", IotApiFuel.calculateAverage(route43));
			data = mapper.writeValueAsString(averageFuel);
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
