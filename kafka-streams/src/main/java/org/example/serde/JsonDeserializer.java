package org.example.serde;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Instant;
import java.util.Map;

public class JsonDeserializer<T> implements Deserializer<T> {

  private final Gson gson = new GsonBuilder()
          .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
          .create();
  private Class<T> deserializedClass;

  public JsonDeserializer(Class<T> deserializedClass) {
    this.deserializedClass = deserializedClass;
  }

  public JsonDeserializer() {
  }

  @Override
  @SuppressWarnings("unchecked")
  public void configure(Map<String, ?> map, boolean b) {
    if (deserializedClass == null) {
      deserializedClass = (Class<T>) map.get("serializedClass");
    }
  }

  @Override
  public T deserialize(String s, byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    return gson.fromJson(new String(bytes), deserializedClass);
  }
}