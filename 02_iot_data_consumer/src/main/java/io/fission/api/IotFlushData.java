package io.fission.api;

import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import io.fission.Context;
import io.fission.Function;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class IotFlushData implements Function  {
	
	private static Logger logger = Logger.getGlobal();
	JedisPool pool = new JedisPool(new JedisPoolConfig(), "redis-single-redis.redis");


	public ResponseEntity call(RequestEntity req, Context context) {
		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			jedis.flushAll();
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
		return ResponseEntity.ok().headers(headers).build();
	}

}
