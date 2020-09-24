package kafka.members;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Consumer {
    //  Membro contenente il topic in cui si metterà a consumer
    private final String topic;

    //  Logger per stampare le operazioni importanti
    private final Logger logger = LoggerFactory.getLogger(Consumer.class.getName());

    //  HashaMap che memorizza
    private final HashMap<String, String> recordKeysAndValues = new HashMap<>();

    /***
     * Costruttore che salva all'interno del membro "topic" il topic passato in ingresso.
     * @param topic
     */
    public Consumer(String topic) {
        this.topic = topic;
    }

    /***
     * Metodo pubblico che viene lanciato sul Consumer, questo metodo crea un ConsumerRunnable che verrà eseguito da un thread.
     */
    public void run() {
        CountDownLatch latch = new CountDownLatch(1);
        logger.info("Creating the consumer thread");

        ConsumerRunnable myConsumerRunnable = new ConsumerRunnable(
                topic,
                latch);

        Thread thread = new Thread(myConsumerRunnable);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook.");
            myConsumerRunnable.shutdown();

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("Application has exited");
        }));
    }

    /***
     * Getter che restituisce l'HashMap contenente i log del topic.
     * @return
     */
    public HashMap<String, String> getRecordKeysAndValues() {
        return recordKeysAndValues;
    }

    /***
     * Classe ConsumerRunnable che fa parte della classe Consumer, essa eredita la classe Runnable, quindi è effettivamente
     * un Runnable ed è possibile effettuare l'override del metodo .run()
     */
    public class ConsumerRunnable implements Runnable {
        private final CountDownLatch latch;
        private final KafkaConsumer<String, String> consumer;

        public ConsumerRunnable(String topic,
                                CountDownLatch latch) {
            this.latch = latch;
            Properties config = getConsumerConfig();
            consumer = new KafkaConsumer<>(config);

            if (!topic.isEmpty()) {
                consumer.subscribe(Collections.singletonList(topic));
            } else {
                logger.info("Error : Topic name is missing!");
            }
        }


        /***
         * Metodo che salva i record del topic in lettura all'interno della HashMap
         */
        @Override
        public void run() {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition; " + record.partition() + ", Offset: " + record.offset());
                        recordKeysAndValues.put(record.key(), record.value());
                    }
                }

            } catch (WakeupException e) {
                logger.info("Received shutdown signal");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        /***
         * Chiama la wakeup() sul consumatore, ovvero interrompe il metodo .poll() ed è in grado di generare WakeUpException
         */
        private void shutdown() {
            consumer.wakeup();
        }

        /***
         * Metodo ausiliario che crea le configurazioni per il consumatore e le restituisce.
         *      -   Server Kafka
         *      -   Key Deserializer per le Stringhe
         *      -   Value Deserializer per le Stringhe
         *      -   Value Deserializer per le Stringhe
         *      -   Nome del group-id
         *      -   AutoOffsetResetConfig, "latest" viene letto l'ultimo log presente all'interno del topic.
         * @return
         */
        private Properties getConsumerConfig() {
            Properties config = new Properties();
            config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "fix-it-employee-app");
            config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

            return config;
        }
    }
}