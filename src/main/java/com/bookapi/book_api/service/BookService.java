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
        // Use the BookRepository's built in methods to save and return the saved book
        return bookRepository.save(newBook);
    }

    public Book updateBook(UUID id, BookInput bookInput) {
        // Find the book to be updated
        Book bookToUpdate = findBookById(id);

        // Update the fields on the book
        bookToUpdate.setTitle(bookInput.getTitle());
        bookToUpdate.setAuthor(bookInput.getAuthor());
        bookToUpdate.setSynopsis(bookInput.getSynopsis());

        // Use the BookRepository's built in methods to save and return the updated book
        return bookRepository.save(bookToUpdate);
    }
}
