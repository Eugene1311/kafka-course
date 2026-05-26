package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.example.model.Payment;
import org.example.model.PaymentPurchase;
import org.example.model.Purchase;
import org.example.serde.PaymentPurchaseSerde;
import org.example.serde.PaymentSerde;
import org.example.serde.PurchaseSerde;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class PaymentPurchaseJoining {
  static void main() {
    Properties props = getProperties();

    StreamsBuilder builder = new StreamsBuilder();

    KStream<Integer, Purchase> purchaseTopicStream = builder.stream("purchases", Consumed.with(Serdes.Integer(), new PurchaseSerde()));
    KStream<Integer, Payment> paymentTopicStream = builder.stream("payments", Consumed.with(Serdes.Integer(), new PaymentSerde()))
            .map((_, payment) -> new KeyValue<>(payment.purchaseId(), payment));

    KStream<Integer, PaymentPurchase> paymentPurchaseStream = purchaseTopicStream.join(
            paymentTopicStream,
            (purchase, payment) -> new PaymentPurchase(purchase.id(), purchase.productName(), payment.status()),
            JoinWindows.ofTimeDifferenceAndGrace(Duration.ofDays(7), Duration.ofHours(24)),
            StreamJoined.with(Serdes.Integer(), new PurchaseSerde(), new PaymentSerde())
    );

    paymentPurchaseStream.to("paymentPurchases", Produced.with(Serdes.Integer(), new PaymentPurchaseSerde()));

    CountDownLatch latch = new CountDownLatch(1);
    KafkaStreams streams = new KafkaStreams(builder.build(), props);
    Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
      @Override
      public void run() {
        streams.close();
        latch.countDown();
      }
    });

    try {
      streams.start();
      latch.await();
    } catch (Throwable e) {
      streams.close();
      System.exit(1);
    }
    System.exit(0);
  }

  private static Properties getProperties() {
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "PaymentPurchaseJoining");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

    // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
    // Note: To re-run the demo, you need to use the offset reset tool:
    // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }
}
