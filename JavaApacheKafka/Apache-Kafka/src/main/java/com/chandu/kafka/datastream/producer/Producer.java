package com.chandu.kafka.datastream.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class Producer {

	public static void main(String[] args) {

		System.out.println("Hello world! from Producer");

		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

		/**
		 * Hard-coding the property values properties.setProperty("value.serializer",
		 * StringSerializer.class.getName()); properties.setProperty("key.serializer",
		 * StringSerializer.class.getName());
		 * properties.setProperty("bootstrap.servers", "BOOTSTRAP_SERVERS");
		 ***/

		// Create the Producer Properties
		Properties properties = new Properties();

		// Using the Apache Producer API constants.
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// Create the Producer
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		// Create the Producer Record
		ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "Hello world");

		// Send the data
		producer.send(record);
		// Flush data

		producer.flush();

		// Flush and close the Producer Data
		producer.close();
	}

}
