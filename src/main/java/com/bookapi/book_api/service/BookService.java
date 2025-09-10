package com.bookapi.book_api.service;

import com.bookapi.book_api.dto.generated.BookInput;
import com.bookapi.book_api.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    public Book createBook(BookInput bookInput) {
        // Create a new Book from the input DTO
        Book newBook = new Book(
                bookInput.getTitle(),
                bookInput.getAuthor(),
                bookInput.getSynopsis()
        );
        // Use the BookRepository's build in methods to save and return the saved book
        return bookRepository.save(newBook);
    }
}
