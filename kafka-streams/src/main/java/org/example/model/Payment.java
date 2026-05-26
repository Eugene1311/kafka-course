package org.example.model;

import java.time.Instant;

public record Payment(
        Integer id,
        String status,
        Instant timestamp,
        Integer purchaseId
) {
}
