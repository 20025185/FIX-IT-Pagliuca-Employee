package kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class StreamRunnable   {

    private CountDownLatch latch;
    private KafkaConsumer<String, String> consumer;
    private KafkaStreams streams;
    private String sourceTopic;
    StreamsBuilder builder;
    private final Logger logger = LoggerFactory.getLogger(FixItStream.class.getName());

    public StreamRunnable(StreamsBuilder builder,
            String topic,
                          CountDownLatch latch) {
        this.sourceTopic = topic;
        this.latch = latch;
        this.builder = builder;

        switch (topic) {
            case "input-ratings":
                input_ratingsStreaming();
                break;
            default:
                logger.info("Error : Missing input topic!");
                break;
        }

        Properties config = getStreamProperties();
        streams = new KafkaStreams(builder.build(), config);

        switch (sourceTopic) {
            case "input-ratings":
                input_ratingsStreaming();
                break;
            default:
                logger.info("Error : Missing input topic!");
                break;
        }

    }

    private Properties getStreamProperties() {
        final Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "employee-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return props;
    }

    private void input_ratingsStreaming() {
        KStream<String, String> inputStream = builder.stream("input-ratings");

        KStream<String, String> favStream = inputStream
                .filter((key, value) -> checkRating(value))
                .selectKey((key, value) -> retrieveIssueType(value).replaceAll(" ", "").toLowerCase())
                .mapValues((key, value) -> getRating(value));

        favStream.to("fav-issues-filter", Produced.with(Serdes.String(), Serdes.String()));

        KStream<String, String> favCountTopics = builder.stream("fav-issues-filter");

        favCountTopics
                .groupByKey()
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("count-store"))
                .toStream()
                .mapValues((key, value) -> Long.toString(value))
                .to("count-fav-issues", Produced.with(Serdes.String(), Serdes.String()));
    }

    public void shutdown() {
        streams.close();
    }

    //  Utils
    private String getRating(String valueJsn) {
        final int RATING_JSON_INDEX = 6;

        List<String> separatedValues;

        String valueStr = valueJsn.substring(1, valueJsn.length() - 1);

        separatedValues = Arrays.asList(valueStr.split(","));

        int end = separatedValues.get(RATING_JSON_INDEX).lastIndexOf("\"");
        int start = separatedValues.get(RATING_JSON_INDEX)
                .substring(0, separatedValues.get(RATING_JSON_INDEX).length() - 1)
                .lastIndexOf("\"");

        String finalStr = separatedValues.get(RATING_JSON_INDEX).substring(start + 1, end);

        double val = Double.parseDouble(finalStr);

        return Double.toString(val);
    }

    private String retrieveIssueType(String valueJsn) {
        final int RATING_JSON_INDEX = 10;

        List<String> separatedValues;

        String valueStr = valueJsn.substring(1, valueJsn.length() - 1);

        separatedValues = Arrays.asList(valueStr.split(","));

        int end = separatedValues.get(RATING_JSON_INDEX).lastIndexOf("\"");
        int start = separatedValues.get(RATING_JSON_INDEX)
                .substring(0, separatedValues.get(RATING_JSON_INDEX).length() - 1)
                .lastIndexOf("\"");

        String finalStr = separatedValues.get(RATING_JSON_INDEX).substring(start + 1, end);

        if (finalStr.equals("Problematica Stradale")) {
            return "STRADALE";
        } else if (finalStr.equals("Problematica di origine naturale")) {
            return "NATURALE";
        } else if (finalStr.equals("Attività sospette")) {
            return "SOSPETTE";
        } else if (finalStr.equals("Altro")) {
            return "ALTRO";
        }
        return "UNDEFINED";
    }

    private KeyValue<String, String> groupByRating(String key, String valueJsn) {
        final int RATING_JSON_INDEX = 6;

        List<String> separatedValues;

        String valueStr = valueJsn.substring(1, valueJsn.length() - 1);

        separatedValues = Arrays.asList(valueStr.split(","));

        int end = separatedValues.get(RATING_JSON_INDEX).lastIndexOf("\"");
        int start = separatedValues.get(RATING_JSON_INDEX)
                .substring(0, separatedValues.get(RATING_JSON_INDEX).length() - 1)
                .lastIndexOf("\"");

        String finalStr = separatedValues.get(RATING_JSON_INDEX).substring(start + 1, end);

        double val = Double.parseDouble(finalStr);

        if (val >= 4.0) {
            separatedValues = new ArrayList<>();
            separatedValues.add(finalStr);

            System.out.println(valueJsn);
            return new KeyValue<String, String>(key, valueJsn);
        }
        return null;
    }


    private boolean checkRatings(String valueJSON) {
        final int RATING_JSON_INDEX = 6;

        List<String> separatedValues;

        String value = valueJSON.substring(1, valueJSON.length() - 1);

        separatedValues = Arrays.asList(value.split(","));

        int end = separatedValues.get(RATING_JSON_INDEX).lastIndexOf("\"");
        int start = separatedValues.get(RATING_JSON_INDEX)
                .substring(0, separatedValues.get(RATING_JSON_INDEX).length() - 1)
                .lastIndexOf("\"");
        String finalStr = separatedValues.get(RATING_JSON_INDEX).substring(start + 1, end);

        double val = Double.parseDouble(finalStr);

        if (val >= 4.0) {
            return true;
        }

        return false;
    }

    private Iterable<?> parseRatings(String value) {
        final int RATING_JSON_INDEX = 6;

        List<String> separatedValues;

        value = value.substring(1, value.length() - 1);

        separatedValues = Arrays.asList(value.split(","));

        int end = separatedValues.get(RATING_JSON_INDEX).lastIndexOf("\"");
        int start = separatedValues.get(RATING_JSON_INDEX)
                .substring(0, separatedValues.get(RATING_JSON_INDEX).length() - 1)
                .lastIndexOf("\"");
        String finalStr = separatedValues.get(RATING_JSON_INDEX).substring(start + 1, end);

        double val = Double.parseDouble(finalStr);

        if (val >= 4.0) {
            separatedValues = new ArrayList<>();
            separatedValues.add(finalStr);
            return separatedValues;
        }

        return null;
    }

    private boolean checkRating(String valueJson) {
        if (valueJson.contains("rating")) {
            int start = valueJson.indexOf("rating") + 9;
            int end = valueJson.indexOf("social");
            double rate = Double.parseDouble(valueJson.substring(start, end - 3));
            return rate >= 4.0;
        }
        return false;
    }

}
