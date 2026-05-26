package org.example.serde;

import org.example.model.Purchase;

public final class PurchaseSerde extends WrapperSerde<Purchase> {
  public PurchaseSerde() {
    super(new JsonSerializer<>(), new JsonDeserializer<>(Purchase.class));
  }
}
