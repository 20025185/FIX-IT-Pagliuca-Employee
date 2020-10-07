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

import java.util.Properties;

@SuppressWarnings("JavaDoc")
public class FixItStream {
    //  Topic fornito in ingresso al costruttore.
    private final String sourceTopic;

    //  StreamsBuilder per la costruzione di uno KStream (o nel caso di una KTable) con il topic fornito in ingresso.
    private final StreamsBuilder builder;

    //  Oggetto di configurazione per lo streaming.
    private final Properties config;

    //  Oggetto che permette di stampare i Log del server di Apache Kafka.
    private final Logger logger = LoggerFactory.getLogger(FixItStream.class.getName());

    /***
     * Costruttore, imposta il topic da cui verrà avviato lo stream.
     * Genera lo StreamsBuilder() e crea le configurazioni da utilizzare con il metodo getStreamProperties().
     * @param topic
     */
    public FixItStream(String topic) {
        this.sourceTopic = topic;
        builder = new StreamsBuilder();
        config = getStreamProperties();
    }

    /***
     * Metodo che effettua un operazione di selezione, in base ad uno dei topic (che deve essere ovviamente presente sul
     * sistema di Apache Kafka) chiama una funzione differente per fare delle operazioni differenti.
     */
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

    /***
     * Metodo che viene chiamatop quando il topic passato come parametro nel costruttore è "input-ratings".
     * Si riceve i dati all'interno di una KStream, successivamente vengono passati all'interno di un altra KStream
     * di nome "favStream" alla quale vengono effettuati dei processamenti :
     *      -   Operazione di filtering utilizzando la funzione di callback checkRating(), si fanno passare solo i report che hanno un rating >= 4.0
     *      -   Mappaggio della chiave del topic con la funzione retrieveIssueType(), in più si effettua un toLowerCase() per assicurarsi che tutti i
     *          caratteri siano uguali in caso di matching futuri.
     *      -   Mappaggio dei valori con la funzione di callback getRating() che restituisce il valore del report.
     *
     * In fine questo nuovo log dopo essere passato in questi tre nodi di processamento viene tutti inviato ad un nodo in uscita
     * (sink) sul topic "fav-issues-filter", per un totale di 5 nodi di processamento (contando anche il nodo di source).
     */
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
                .mapValues((key, value) ->Long.toString(value))
                .to("count-fav-issues", Produced.with(Serdes.String(), Serdes.String()));

        final Topology topology = builder.build();
        System.out.println(topology.describe());

        KafkaStreams streams = new KafkaStreams(topology, config);

        streams.start();
        streams.localThreadsMetadata().forEach(data -> System.out.println());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /***
     * Funzione ausiliaria che setta l'oggetto di tipo Properties con tutte le impostazioni per rendere lo stream
     * utile per lo scopo.
     *      -   Nome dell'id dell'applicazione
     *      -   Indirizzo broker
     *      -   Key Serializer/Deserializer
     *      -   Value Serializer/Deserializer
     *      -   Auto_Offset_Reset_Config -> I log vengono letti dal più recente.
     * @return
     */
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

    /***
     * Metodo Callback utilizzato per restituire un tag rappresentate il tipo della segnalazione, questo perchè restituire
     * esattamente la stringa del tipo potrebbe risultare in svariate complicanze in fase di matching (es. se si perde un carattere).
     * @param valueJsn
     * @return
     */
    private String retrieveIssueType(String valueJsn) {
        String valueStr = valueJsn.substring(1, valueJsn.length() - 1);
        int start = valueStr.indexOf("type");
        String typeStr = valueStr.substring(start + 7, valueStr.length() - 1);
        int end = typeStr.indexOf(",");
        typeStr = typeStr.substring(0, end - 1);

        switch (typeStr) {
            case "Problematica Stradale":
                return "stradale";
            case "Problematica di origine naturale":
                return "naturale";
            case "Attività sospette":
                return "sospette";
            case "Altro":
                return "altro";
            default:
                return "undefined";
        }
    }

    /***
     *  Funzione di callback che restituisce il valore della recensione nella segnalazione rappresentata da un oggetto Json.
     * @param valueJsn
     * @return
     */
    private String getRating(String valueJsn) {
        String valueStr = valueJsn.substring(1, valueJsn.length() - 1);
        int start = valueStr.indexOf("rating");
        String typeStr = valueStr.substring(start + 9, valueStr.length() - 1);
        int end = typeStr.indexOf("social") - 3;
        typeStr = typeStr.substring(0, end);
        System.out.println(typeStr);

        return typeStr;
    }

    /***
     * Funzione Callback utilizzata per effettuare il filtering delle segnalazioni, vengono fatte passare
     * soltanto le segnalazioni che hanno una recensione >= 4.0
     * @param valueJson
     * @return
     */
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
