package kafka.streams;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducersSimulation {
    @SuppressWarnings("BusyWait")
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        /*  Configurazione degli ack, ack=none -> il broker non risponde con ack,
            ack=leader il kafka broker risponde con un ack che conferma che la partizione leader ha salvato il record.
            ack=leader non è affidabile al 100% ,perchè se il leader fallisse subito dopo aver inviato l'ack il record potrebbe essere perso prima che gli ISR lo duplichino.
            ack=all, significa che il leader riceve la conferma di write da tutte le ISR prima di inviare un ack al producer. garantisce che il record non sia stato perso finchè non
            muore un ISR.      */
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        //  In caso di fallimento il producer proverà a rinviare altre 3 volte
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");

        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");   //   no in production
        //  Abilita l'idempotenza che serve per l'exactly-once
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");   //

        Producer<String, String> producer = new KafkaProducer<>(properties);

        int i = 0;
        while (true) {
            System.out.println("Producing batch: " + i);
            try {
                producer.send(newRandomTransaction("Problematica di origine naturale"));
                Thread.sleep(10);

                producer.send(newRandomTransaction("Problematica di origine naturale"));
                Thread.sleep(10);

                producer.send(newRandomTransaction("Altro"));
                Thread.sleep(10);

                producer.send(newRandomTransaction("Altro"));
                Thread.sleep(10);

                producer.send(newRandomTransaction("Attività sospette"));
                Thread.sleep(10);

                producer.send(newRandomTransaction("Problematica Stradale"));
                Thread.sleep(10);

                i += 1;
            } catch (InterruptedException e) {
                break;
            }
        }
        producer.close();
    }

    public static ProducerRecord<String, String> newRandomTransaction(String type) {
        ObjectNode transaction = JsonNodeFactory.instance.objectNode();

        transaction.put("key", "Tutt hanno bisogno di una chiave...");
        transaction.put("description", "Una descrizione banale.");
        transaction.put("id", "MR.X");
        transaction.put("object", "UN OGGETTO");
        transaction.put("position", "UN OGGETTO");
        transaction.put("priority", "0");
        transaction.put("rating", "4.5");
        transaction.put("social", "false");
        transaction.put("status", "Chiusa");
        transaction.put("time", "4:30");
        transaction.put("type", type);
        transaction.put("uid", "Non si può sapere l'uid di mr.x");

        return new ProducerRecord<>("input-ratings", type, transaction.toString());
    }
}
