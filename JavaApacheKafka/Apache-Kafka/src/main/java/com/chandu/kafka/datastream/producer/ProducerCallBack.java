package com.chandu.kafka.datastream.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerCallBack {

	public static void main(String[] args) {

		Logger logger = LoggerFactory.getLogger(ProducerCallBack.class);

		System.out.println("Hello world! from Producer CallBack");

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

		for (int i = 0; i < 10; i++) {

			// Create the Producer Record
			ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic",
					"Hello world" + Integer.toString(i));

			// Send the data asynchronous
			producer.send(record, new Callback() {

				@Override
				public void onCompletion(RecordMetadata metadata, Exception e) {
					// executes every time a record is successfully sent or an exception is thrown
					if (e == null) {
						// If the record was successfully sent
						logger.info("Recieved new Metadata: \n" + "Topic :" + metadata.topic() + "\n" + "Paritition : "
								+ metadata.partition() + "\n" + "Offset : " + metadata.offset() + "\n" + "TimeStamp : "
								+ metadata.timestamp());

					} else {
						logger.error("Error While Producing" + e);

					}

				}

			});
			// Flush data
			producer.flush();
		}

		// Flush and close the Producer Data
		producer.close();
	}

}
