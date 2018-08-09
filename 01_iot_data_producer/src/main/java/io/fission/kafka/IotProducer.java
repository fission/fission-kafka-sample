package io.fission.kafka;

import io.fission.Function;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import io.fission.kafka.IoTData;

import io.fission.Context;

public class IotProducer implements Function {

	private static Logger logger = Logger.getGlobal();
	static final long FIVE_MINUTE_IN_MILLIS=300000;//millisecs

	
	public ResponseEntity call(RequestEntity req, Context context) {
		
		String brokerList = System.getenv("KAFKA_ADDR");
		String topic = System.getenv("TOPIC_NAME");
		if (brokerList == null || topic == null) {
			return ResponseEntity.badRequest().build();
		}
		
		// Related issue: https://stackoverflow.com/questions/37363119/kafka-producer-org-apache-kafka-common-serialization-stringserializer-could-no
		Thread.currentThread().setContextClassLoader(null);
		
		Properties properties = new Properties();
		properties.put("bootstrap.servers", brokerList);
		properties.put("acks", "all");
		properties.put("value.serializer", "io.fission.kafka.IoTDataEncoder");
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		Producer<String, IoTData> producer = new KafkaProducer<String, IoTData>(properties);
		IotProducer iotProducer = new IotProducer();
		try {
			iotProducer.generateIoTEvent(producer,topic);
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.severe("Failed to send events to Kafka"+ e);
		}
		producer.close();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Headers", "*");
		headers.add("Access-Control-Allow-Credentials", "true");
		headers.add("Access-Control-Allow-Methods", "*");
		headers.add("Access-Control-Expose-Headers", "*");
		return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
	}

	/**
	 * Method runs 100s of times and generates random IoT data in JSON with below
	 * format.
	 * 
	 * {"vehicleId":"52f08f03-cd14-411a-8aef-ba87c9a99997","vehicleType":"Public
	 * Transport","routeId":"route-43","latitude":",-85.583435","longitude":"38.892395","timestamp":1465471124373,"speed":80.0,"fuelLevel":28.0}
	 * 
	 * @throws InterruptedException
	 * 
	 */
	private void generateIoTEvent(Producer<String, IoTData> producer, String topic) throws InterruptedException {
		List<String> routeList = Arrays.asList(new String[] { "Route-37", "Route-43", "Route-82" });
		List<String> vehicleTypeList = Arrays
				.asList(new String[] { "Large Truck", "Small Truck", "Van", "18 Wheeler", "Car" });
		Random rand = new Random();
		logger.info("Sending events");
		// generate event in loop
		List<IoTData> eventList = new ArrayList<IoTData>();
		for (int i = 0; i < 10; i++) {// create 1000 vehicles and push to Kafka on every function invocation
			String vehicleId = UUID.randomUUID().toString();
			String vehicleType = vehicleTypeList.get(rand.nextInt(5));
			String routeId = routeList.get(rand.nextInt(3));
			

			Calendar d1 = Calendar.getInstance();
			long t = d1.getTimeInMillis();
			Date d2 = new Date(t + (FIVE_MINUTE_IN_MILLIS));
			Date timestamp =  new Date(ThreadLocalRandom.current().nextLong(t, d2.getTime()));
			
			double speed = rand.nextInt(100 - 20) + 20;// random speed between 20 to 100
			double fuelLevel = rand.nextInt(40 - 10) + 10;
			for (int j = 0; j < 5; j++) {// Add 5 events for each vehicle
				String coords = getCoordinates(routeId);
				String latitude = coords.substring(0, coords.indexOf(","));
				String longitude = coords.substring(coords.indexOf(",") + 1, coords.length());
				IoTData event = new IoTData(vehicleId, vehicleType, routeId, latitude, longitude, timestamp, speed,
						fuelLevel);
				eventList.add(event);
			}
		}
		Collections.shuffle(eventList);// shuffle for random events
		for (IoTData event : eventList) {
			producer.send(new ProducerRecord<String, IoTData>(topic, event));
		}

	}

	// Method to generate random latitude and longitude for routes
	private String getCoordinates(String routeId) {
		Random rand = new Random();
		int latPrefix = 0;
		int longPrefix = -0;
		if (routeId.equals("Route-37")) {
			latPrefix = 33;
			longPrefix = -96;
		} else if (routeId.equals("Route-82")) {
			latPrefix = 34;
			longPrefix = -97;
		} else if (routeId.equals("Route-43")) {
			latPrefix = 35;
			longPrefix = -98;
		}
		Float lati = latPrefix + rand.nextFloat();
		Float longi = longPrefix + rand.nextFloat();
		return lati + "," + longi;
	}

}