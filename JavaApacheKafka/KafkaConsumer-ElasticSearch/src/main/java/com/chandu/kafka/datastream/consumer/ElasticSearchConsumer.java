package com.chandu.kafka.datastream.consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class ElasticSearchConsumer {

	static Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());

	public static RestHighLevelClient createClient() {

		// Replace with your own Credentials
		String hostname = "kafka-stream-8077083392.us-east-1.bonsaisearch.net";
		String username = "vsa6i6uglz";
		String password = "spupc0yws2";

		// DO NOT Create if you run a Local Elastic Search - Use this if you are running
		// in the Cloud
		final CredentialsProvider credentialProvider = new BasicCredentialsProvider();
		credentialProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

		RestClientBuilder builder = RestClient.builder(new HttpHost(hostname, 443, "https"))
				.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setDefaultCredentialsProvider(credentialProvider);

					}
				});
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;

	}

	public static KafkaConsumer<String, String> createConsumer(String topic) {
		// Create the Consumer Configuration
		Properties properties = new Properties();

		String BOOTSTRAP_SERVERS = "127.0.0.1:9092";
		String groupId = "kafka-elasticsearch";

		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Disable auto commit of Offsets
		properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20"); // limiting the max poll records to 10.
																				// //
																				// (Default is true)

		// create consumer
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
		consumer.subscribe(Arrays.asList(topic));
		return consumer;

	}

	public static void main(String[] args) throws IOException {

		RestHighLevelClient client = createClient();
		// String jsonString = "{ \"team\": \"IPL\"}";

//		IndexRequest indexRequest = new IndexRequest("twitter", "tweets").source(jsonString, XContentType.JSON);
//
//		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
//		String id = indexResponse.getId();
//		logger.info(id);

		KafkaConsumer<String, String> consumer = createConsumer("twitter_tweets");
		while (true) {
			// consumer.poll(100); // new in Kafka 2.0.0 used to take long, now we need to
			// use the Duration as below

			ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
			Integer recordCount = records.count();

			logger.info("Received:" + recordCount + "records");

			// Batching the requests
			BulkRequest bulkRequest = new BulkRequest();

			for (ConsumerRecord<String, String> record : records) {
				// where we insert the data into the Elastic Search

				// strategies for Idempotent
				// Kafka Generic ID
				// String id = record.topic() + "_" + record.partition() + "_" +
				// record.offset();

				// Twitter feed specific id
				try {
					String id = extractIdFromTweet(record.value());
					// id here in IndexRequest Parameter is used to make the consumer idempotent
					IndexRequest indexRequest = new IndexRequest("twitter", "tweets", id).source(record.value(),
							XContentType.JSON);
					bulkRequest.add(indexRequest); // we add to our bulk request that takes no time.
				} catch (NullPointerException e) {
					logger.warn("Skipping Bad Data" + record.value());

				}

				// IndexResponse indexResponse = client.index(indexRequest,
				// RequestOptions.DEFAULT);
				// logger.info(indexResponse.getId());
//				try {
//					Thread.sleep(1000);// Introduce a small delay
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

			}

			if (recordCount > 0) {
				BulkResponse bulkresponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
				logger.info("Committing Offsets....");
				consumer.commitSync();
				logger.info("Offsets have been committed");

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		// Close the Client gracefully.
		// client.close();

	}

	private static JsonParser jsonParser = new JsonParser();

	private static String extractIdFromTweet(String tweetJson) {
		// Gson Library
		return jsonParser.parse(tweetJson).getAsJsonObject().get("id_str").getAsString();

	}

}
