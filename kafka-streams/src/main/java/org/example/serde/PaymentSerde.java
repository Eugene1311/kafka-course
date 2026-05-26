package org.example.serde;

import org.example.model.Payment;

public final class PaymentSerde extends WrapperSerde<Payment> {
  public PaymentSerde() {
    super(new JsonSerializer<>(), new JsonDeserializer<>(Payment.class));
  }
}
