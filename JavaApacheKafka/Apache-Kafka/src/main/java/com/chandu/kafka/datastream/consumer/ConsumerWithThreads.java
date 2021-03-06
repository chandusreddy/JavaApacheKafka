package com.chandu.kafka.datastream.consumer;

import java.time.Duration;
import java.util.Arrays;
//import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerWithThreads {

	private ConsumerWithThreads() {

	}

	private void run() {
		Logger logger = LoggerFactory.getLogger(ConsumerWithThreads.class);

		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
		String groupId = "my-sixth-application";
		String topic = "first_topic";
		// Count Down Latch for dealing with Multiple Threads
		CountDownLatch latch = new CountDownLatch(1);
		// Create the consumer Runnable
		logger.info("Creating the Consumer Thread: ");
		Runnable myConsumerRunnable = new ConsumerRunnable(BOOTSTRAP_SERVERS, groupId, topic, latch);

		// Start the thread
		Thread myThread = new Thread(myConsumerRunnable);
		myThread.start();

		// Add shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Caught Shutdown Hook");
			((ConsumerRunnable) myConsumerRunnable).shutdown();
			try {
				latch.await();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			logger.info("Application has exited: ");

		}));

		try {
			latch.await();

		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("Application got Interrupted");
		} finally {
			logger.info("Application is closing:");

		}

	}

	public static void main(String[] args) {
		System.out.println("Hello world from Consumer: ");
		new ConsumerWithThreads().run();

	}

	public class ConsumerRunnable implements Runnable {

		private CountDownLatch latch;
		private KafkaConsumer<String, String> consumer;
		private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

		public ConsumerRunnable(String BOOTSTRAP_SERVERS, String groupId, String topic, CountDownLatch latch) {

			this.latch = latch;

			// Create the Consumer Configuration
			Properties properties = new Properties();
			properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
			properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
			properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

			// create consumer
			consumer = new KafkaConsumer<>(properties);
			// subscribe consumer to our topics
			consumer.subscribe(Arrays.asList(topic));
		}

		@Override
		public void run() {
			try {
				// poll for the new data
				while (true) {
					// consumer.poll(100); // new in Kafka 2.0.0 used to take long, now we need to
					// use the Duration as below

					ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

					for (ConsumerRecord<String, String> record : records) {
						logger.info("Key: " + record.key(), "Value: " + record.value());
						logger.info("Partition: " + record.partition() + "Offset: " + record.offset());

					}

				}
			} catch (WakeupException e) {
				logger.info("Recieved Shutdown Signal!");

			} finally {
				consumer.close();
				// Tell our main code that we are done with the consumer
				latch.countDown();
			}

		}

		public void shutdown() {
			// The wakeup() method is a special method to interrupt consumer.poll()
			// It will throw the exception WakeUpException
			consumer.wakeup();
		}

	}
}
