package com.bookapi.book_api.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private UUID id;

    @Indexed(unique = true)
    private String email;

    private String name;
    private String role;

    public User(String email, String name, String role) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.name = name;
        this.role = role;
    }
}
