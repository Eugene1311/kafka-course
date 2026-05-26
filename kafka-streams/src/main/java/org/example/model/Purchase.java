package org.example.model;

import java.time.Instant;

public record Purchase(
        Integer id,
        String productName,
        Instant timestamp
) {
}
