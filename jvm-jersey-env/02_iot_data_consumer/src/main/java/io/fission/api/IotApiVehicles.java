package io.fission.api;

import io.fission.Context;
import io.fission.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.container.ContainerRequestContext;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotApiVehicles implements Function<ContainerRequestContext,Response>  {
	
	private static Logger logger = Logger.getGlobal();
	final ObjectMapper mapper = new ObjectMapper();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_ADDR"));

	public Response call(ContainerRequestContext req, Context context) {
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
		
		MultivaluedMap<String,Object> headers = new MultivaluedHashMap<String,Object>();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
		headers = IotApiFuel.AddCorsHeaders(headers);
		return Response.ok(data).replaceAll(headers).build();
	}

}
