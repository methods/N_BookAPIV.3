package com.bookapi.book_api.service;

import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    // Constructor injection to get the repository
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book findBookById(UUID id) {
        // Use the repository to find the book
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }
}
