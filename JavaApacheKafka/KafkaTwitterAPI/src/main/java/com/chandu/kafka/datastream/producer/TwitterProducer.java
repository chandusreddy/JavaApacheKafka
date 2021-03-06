package com.chandu.kafka.datastream.producer;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

public class TwitterProducer {
	Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());
	String consumerKey = "YlpKuRj31kJ5X2nRKTm7BzDN7";
	String consumerSecret = "gvBr2C7stQ8mUcO6BHgq56OMr6BHqjlJQtyugOhK6Vuv1007sZ";
	String token = "1379870073335218182-NcpQSpbmJERlNqued0y10xdamgsq3W";
	String secret = "LjFrIcCJRwUlZ24FqQXfrZfHBvASrNoRCCk2mTgAonf3G";

	List<String> terms = Lists.newArrayList("Sachin", "IPL", "Java");

	public TwitterProducer() {

	}

	public static void main(String[] args) {
		new TwitterProducer().run();
		System.out.println("Hello world");

	}

	public void run() {
		logger.info("Application Setup");
		/**
		 * Set up your blocking queues: Be sure to size these properly based on expected
		 * TPS of your stream
		 */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);

		// Create the twitter Client
		Client client = createTwitterClient(msgQueue);
		// Attempts to establish a connection.
		client.connect();

		// Create a Kafka Producer
		KafkaProducer<String, String> producer = createKafkaProducer();

		// Add a shutdown Hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			logger.info("Stopping the Application..");
			logger.info("Shutting Down client from Twitter");
			client.stop();
			logger.info("Closing Producer");
			producer.close();
			logger.info("Done!!");

		}));

		// Loop to send tweets to Kafka
		// on a different thread, or multiple different threads....
		while (!client.isDone()) {
			String msg = null;
			try {
				msg = msgQueue.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				client.stop();
			}
			if (msg != null) {
				logger.info(msg);
				producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {

					@Override
					public void onCompletion(RecordMetadata metadata, Exception e) {
						if (e != null) {
							logger.error(
									"Something went Wrong, Please try again or check the logs for additional Information ",
									e);
						}
					}

				});

			}

		}
		logger.info("End of Application");
	}

	public Client createTwitterClient(BlockingQueue<String> msgQueue) {

		/**
		 * Declare the host you want to connect to, the end point, and authentication
		 * (basic auth or oauth )
		 */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

		// Optional: set up some followings and track terms

		hosebirdEndpoint.trackTerms(terms);

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

		ClientBuilder builder = new ClientBuilder().name("Hosebird-Client-01") // optional: mainly for the logs
				.hosts(hosebirdHosts).authentication(hosebirdAuth).endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue));

		Client hosebirdClient = builder.build();
		return hosebirdClient;

	}

	public KafkaProducer<String, String> createKafkaProducer() {
		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

		// Create the Producer Properties
		Properties properties = new Properties();

		// Using the Apache Producer API constants.
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		// creating Safe and efficient Producer
		properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
		properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
		properties.setProperty(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
		properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5"); // Kafka 2.0 >=1 so we can
																							// keep this as 5. Use 1
																							// otherwise
		// HighThroughput Producer Configurations
		properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
		properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
		properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));// 32 KB batch size and the Default is 16 KB.
		
		KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

		return producer;

	}

	{

	}
}
