package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.model.Payment;
import org.example.model.Purchase;
import org.example.serde.InstantTypeAdapter;
import org.example.serde.JsonSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Slf4j
public class DataGenerator {
  static void main() {
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();

    Properties properties = new Properties();
    properties.put("bootstrap.servers", "localhost:9092");
    properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");

    Properties purchaseProperties = new Properties(properties);
    purchaseProperties.put("value.serializer", JsonSerializer.class);

    Properties paymentProperties = new Properties(properties);
    paymentProperties.put("value.serializer", JsonSerializer.class);

    try (
            FileInputStream purchaseInputStream = new FileInputStream(Paths.get("src/main/resources/purchases.json").toFile());
      Producer<Integer, Purchase> purchaseProducer = new KafkaProducer<>(purchaseProperties);
      FileInputStream paymentsInputStream = new FileInputStream("src/main/resources/payments.json");
      Producer<Integer, Payment> paymentsProducer = new KafkaProducer<>(paymentProperties);
    ) {
      var purchases = new String(purchaseInputStream.readAllBytes());
      List<ProducerRecord<Integer, Purchase>> purchaseRecords = Arrays.stream(gson.fromJson(purchases, Purchase[].class))
              .map(p -> new ProducerRecord<>("purchases", p.id(), p))
              .toList();
      purchaseRecords.forEach(record -> purchaseProducer.send(record, (_, e) -> {
        if (e != null) {
          log.error(e.getMessage(), e);
        }
      }));

      var payments = new String(paymentsInputStream.readAllBytes());
      List<ProducerRecord<Integer, Payment>> paymentsRecords = Arrays.stream(gson.fromJson(payments, Payment[].class))
              .map(p -> new ProducerRecord<>("payments", p.id(), p))
              .toList();
      paymentsRecords.forEach(record -> paymentsProducer.send(record, (_, e) -> {
        if (e != null) {
          log.error(e.getMessage(), e);
        }
      }));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
