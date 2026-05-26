package org.example.serde;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

public class JsonSerializer<T> implements Serializer<T> {

  private final Gson gson = new GsonBuilder()
          .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
          .create();;

  @Override
  public void configure(Map<String, ?> map, boolean b) {

  }

  @Override
  public byte[] serialize(String topic, T t) {
    return gson.toJson(t).getBytes(StandardCharsets.UTF_8);
  }
}
