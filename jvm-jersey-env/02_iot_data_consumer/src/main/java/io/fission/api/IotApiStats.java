package io.fission.api;

import io.fission.Context;
import io.fission.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotApiStats implements Function<ContainerRequestContext,Response>  {
	
	private static Logger logger = Logger.getGlobal();
	final ObjectMapper mapper = new ObjectMapper();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_ADDR"));

	public Response call(ContainerRequestContext req, Context context) {
		Jedis jedis = null;
		HashMap<String,String> statCount = new HashMap<String,String>();
		String data = "";
		try {
			jedis = pool.getResource();
			statCount.put("TOTAL_MSG_SENT", jedis.hget("RECORD_SENT_BY_PRODUCER", "COUNT"));
			statCount.put("TOTAL_MSG_PROCESSED", jedis.hget("RECORD_ACK_BY_CONSUMER", "COUNT"));
			data = mapper.writeValueAsString(statCount);
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
