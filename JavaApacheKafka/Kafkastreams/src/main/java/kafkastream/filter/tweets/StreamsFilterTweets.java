package kafkastream.filter.tweets;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import com.google.gson.JsonParser;

public class StreamsFilterTweets {

	public static void main(String[] args) {

		// create properties
		Properties properties = new Properties();
		properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9002");
		properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "demo-kafka-streams");
		properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
		properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());

		// create a topology
		StreamsBuilder streamsBuilder = new StreamsBuilder();

		// input topic
		KStream<String, String> inputTopic = streamsBuilder.stream("twitter_tweets");
		KStream<String, String> filteredStream = inputTopic
				.filter((k, jsonTweet) -> extractUserFollowersInTweets(jsonTweet) > 10000);
		filteredStream.to("important_tweets");

// filter for tweets which has a  user of over 10000 followers

		// build the topology
		KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), properties);
		// start our streams application
		kafkaStreams.start();

	}

	private static JsonParser jsonParser = new JsonParser();

	private static Integer extractUserFollowersInTweets(String tweetJson) {
		// Gson Library
		try {
			return jsonParser.parse(tweetJson).getAsJsonObject().get("user").getAsJsonObject().get("followers_count")
					.getAsInt();
		} catch (NullPointerException e) {
			return 0;
		}

	}

}
