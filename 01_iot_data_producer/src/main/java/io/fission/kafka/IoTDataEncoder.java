package io.fission.kafka;


import java.util.Map;
import java.util.logging.Logger;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fission.kafka.IoTData;


/**
 * Class to convert IoTData java object to JSON String
 * 
 * @author abaghel
 *
 */
public class IoTDataEncoder implements Serializer<IoTData> {
	
	private static Logger logger = Logger.getGlobal();
	
	private static ObjectMapper objectMapper = new ObjectMapper();
	
	public IoTDataEncoder() {

    }
	
	public void configure(Map<String, ?> configs, boolean isKey) {		
	}
	
	public byte[] serialize(String topic, IoTData data) {
		try {
			String msg = objectMapper.writeValueAsString(data);
			logger.info(msg);
			return msg.getBytes();
		} catch (JsonProcessingException e) {
			logger.severe("Error in Serialization" + e);
		}
		return null;
	}
	
	public void close() {
	}
}
