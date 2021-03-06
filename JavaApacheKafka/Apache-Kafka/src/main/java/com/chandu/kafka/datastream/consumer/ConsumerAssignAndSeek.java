package com.chandu.kafka.datastream.consumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerAssignAndSeek {

	public static void main(String[] args) {
		System.out.println("Hello world from ConsumerAssignAndSeek: ");

		Logger logger = LoggerFactory.getLogger(ConsumerAssignAndSeek.class);

		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
		String topic = "first_topic";

		// Create the Consumer Configuration
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		// create consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

		// Assign and seek are mostly used to replay data or fetch a specific message

		// assign
		TopicPartition paritionToReadFrom = new TopicPartition(topic, 0);
		long offsetToReadFrom = 2L;
		consumer.assign(Arrays.asList(paritionToReadFrom));

		// seek
		consumer.seek(paritionToReadFrom, offsetToReadFrom);
		
		int numberOfMessagesToRead = 5;
		boolean keepOnReading = true;
		int numberOfMessagesReadSofar = 0;

		// poll for the new data
		while (keepOnReading) {
			// consumer.poll(100); // new in Kafka 2.0.0 used to take long, now we need to
			// use the Duration as below

			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

			for (ConsumerRecord<String, String> record : records) {
				numberOfMessagesReadSofar +=1;
				logger.info("Key: " + record.key(), "Value: " + record.value());
				logger.info("Partition: " + record.partition() + "Offset: " + record.offset());
				
				if(numberOfMessagesReadSofar >= numberOfMessagesToRead) {
					keepOnReading = false; // to exit the while loop
					break; // to exit the for loop
				}

			}

		}
		logger.info("Exiting the Application: ");

	}

}
