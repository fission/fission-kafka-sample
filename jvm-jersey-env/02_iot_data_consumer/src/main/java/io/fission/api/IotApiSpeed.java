package io.fission.api;

import io.fission.Context;
import io.fission.Function;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotApiSpeed implements Function<ContainerRequestContext,Response>  {
	
	private static Logger logger = Logger.getGlobal();
	final ObjectMapper mapper = new ObjectMapper();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_ADDR"));


	public Response call(ContainerRequestContext req, Context context) {
		Jedis jedis = null;
		HashMap<String, Integer> averageFuel = new HashMap<String, Integer>();
		String data = "";
		try {
			jedis = pool.getResource();
			List<String> route82 =  jedis.lrange("ROUTE-82-SPEED", 0, 1999);
			List<String> route37 =  jedis.lrange("ROUTE-37-SPEED", 0, 1999);
			List<String> route43 =  jedis.lrange("ROUTE-43-SPEED", 0, 1999);
			
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
		
		MultivaluedMap<String,Object> headers = new MultivaluedHashMap<String,Object>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		headers = IotApiFuel.AddCorsHeaders(headers);
		return Response.ok(data).replaceAll(headers).build();
	}

}
