package com.chandu.kafka.datastream.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Consumer {

	public static void main(String[] args) {
		System.out.println("Hello world from Consumer: ");

		Logger logger = LoggerFactory.getLogger(Consumer.class);

		// Create the Consumer Configuration
		Properties properties = new Properties();

		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
		String groupId = "my-fourth-application";
		String topic = "first_topic";

		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// create consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

		// Subscribe the consumer to the topic(s)
		consumer.subscribe(Collections.singleton(topic));

		// Subscribe to multiple topics using the Arrays
		// consumer.subscribe(Arrays.asList(topic, topic1));

		// poll for the new data
		while (true) {
			// consumer.poll(100); // new in Kafka 2.0.0 used to take long, now we need to use the Duration as below
			
			ConsumerRecords<String,String> records = consumer.poll(Duration.ofMillis(100));
			
			for(ConsumerRecord<String,String> record : records) {
				logger.info("Key: " + record.key(), "Value: " + record.value());
				logger.info("Partition: " +record.partition() + "Offset: " + record.offset());
				
				
			}

		}

	}

}
