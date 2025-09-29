package com.bookapi.book_api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "reservations")
public class Reservation {

    @Id
    private UUID id;
    private UUID bookId;
    private UUID userId;
    private String state;
    private Instant reservedAt;

    public Reservation(UUID bookId, UUID userId) {
        this.id = UUID.randomUUID();
        this.bookId = bookId;
        this.userId = userId;
        this.state = "Reserved";
        this.reservedAt = Instant.now();
    }

}
