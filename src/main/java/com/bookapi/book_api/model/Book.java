package com.bookapi.book_api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private UUID id;

    private String title;
    private String author;
    private String synopsis;

    public Book(String title, String author, String synopsis) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.synopsis = synopsis;
        this.author = author;
    }
}
