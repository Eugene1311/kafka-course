package org.example.serde;

import org.example.model.PaymentPurchase;

public final class PaymentPurchaseSerde extends WrapperSerde<PaymentPurchase> {
  public PaymentPurchaseSerde() {
    super(new JsonSerializer<>(), new JsonDeserializer<>(PaymentPurchase.class));
  }
}
