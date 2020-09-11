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
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Consumer {
    private final String topic;

    private final Logger logger = LoggerFactory.getLogger(Consumer.class.getName());

    public Consumer(String topic) {
        this.topic = topic;
    }

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

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted. " + e);
            e.printStackTrace();
        } finally {
            logger.info("Application is closing");
        }

    }

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

        public Properties getConsumerConfig() {
            Properties config = new Properties();
            config.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            config.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            config.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            String groupId = "fix-it-employee-app";
            config.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");    //  latest, none

            return config;
        }

        @Override
        public void run() {
            try {
                //  poll for new data
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key: " + record.key() + ", Value: " + record.value());
                        logger.info("Partition; " + record.partition() + ", Offset: " + record.offset());
                    }
                }
            } catch (WakeupException e) {
                logger.info("Received shutdown signal");
            } finally {
                consumer.close();
                latch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();  //  Interrupt the .poll(), it will throw the WakeUpException
        }
    }
}
