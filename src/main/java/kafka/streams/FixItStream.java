package kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class FixItStream {
    private final String sourceTopic;
    private final StreamsBuilder builder;
    private final Properties config;

    private final Logger logger = LoggerFactory.getLogger(FixItStream.class.getName());

    public FixItStream(String topic) {
        this.sourceTopic = topic;
        builder = new StreamsBuilder();
        config = getStreamProperties();

    }

    public void execute() {
        switch (sourceTopic) {
            case "input-ratings":
                input_ratingsStreaming();
                break;
            case "another-topic":
                System.out.println("inserire funzione topic");
                break;
            default:
                logger.info("Error : Missing input topic!");
                break;
        }
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
                .count(Materialized.as("count-store"))
                .toStream()
                .mapValues((key, value) -> Long.toString(value))
                .to("count-fav-issues", Produced.with(Serdes.String(), Serdes.String()));

        final Topology topology = builder.build();
        System.out.println(topology.describe());


        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        //streams.cleanUp();
        streams.start();
        streams.localThreadsMetadata().forEach(data -> System.out.println());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
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

        switch (finalStr) {
            case "Problematica Stradale":
                return "STRADALE";
            case "Problematica di origine naturale":
                return "NATURALE";
            case "Attività sospette":
                return "SOSPETTE";
            case "Altro":
                return "ALTRO";
        }
        return "UNDEFINED";
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
