package com.bookapi.book_api.repository;

import com.bookapi.book_api.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends MongoRepository<Book, UUID> {
}
