package kafka.streams;

import org.apache.kafka.streams.StreamsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


public class FixItStream {
    private String sourceTopic = "";

    private final Logger logger = LoggerFactory.getLogger(FixItStream.class.getName());

    public FixItStream(String topic) {
        sourceTopic = topic;
       /* Runnable runnableStream = this::streamsRunnable;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(runnableStream, 0, 100, TimeUnit.MILLISECONDS);
*/
    }

    public void run() {
        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating the kafka stream");
        StreamsBuilder builder = new StreamsBuilder();
        StreamRunnable myStreamRunnable = new StreamRunnable(builder, sourceTopic, latch);

        /*Thread thread = new Thread(myStreamRunnable);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Caught shutdown hook.");

            myStreamRunnable.shutdown();

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("Stream has exited");
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Stream got interrupted. " + e);
            e.printStackTrace();
        } finally {
            logger.info("Stream is closing");
        }*/

    }


}
