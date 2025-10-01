package com.bookapi.book_api.controller;

import com.bookapi.book_api.controller.generated.BooksApi;
import com.bookapi.book_api.dto.generated.BookInput;
import com.bookapi.book_api.dto.generated.BookListResponse;
import com.bookapi.book_api.dto.generated.BookOutput;
import com.bookapi.book_api.mapper.BookMapper;
import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
public class BookController implements BooksApi {

    private final BookService bookService;
    private final BookMapper bookMapper;

    // Use constructor injection for all dependencies
    public BookController (BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookOutput> addBook(BookInput bookInput) {
        // Call the BookService method for adding a book
        Book createdBook = bookService.createBook(bookInput);
        // Build the URI to be returned with the 201 response
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBook.getId())
                .toUri();
        // Use the mapper to convert it to a public DTO
        BookOutput bookOutput = bookMapper.toBookOutput(createdBook);
        // Return the DTO with a 201 Created Status
        return ResponseEntity.created(location).body(bookOutput);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBookById(UUID bookId) {
        // Call the service function
        Book deletedBook = bookService.deleteBook(bookId);
        // Return 204 No Content
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<BookListResponse> getAllBooks(Integer offset, Integer limit) {
        // Create a pageable object from offset and limit
        // Calculate the page by dividing offset by limit
        int page = limit > 0 ? offset / limit : 0; // If limit is passed as 0 or <0, set to 0
        Pageable pageable = PageRequest.of(page, limit);

        // Call the service function
        Page<Book> bookPage = bookService.findAllBooks(pageable);

        // Use the mapper to convert the Page to a list of DTOs
        BookListResponse response = bookMapper.toBookListResponse(bookPage);

        // Return 200 OK with the list
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<BookOutput> getBookById(UUID bookId) {
        // Call the service function
        Book foundBook = bookService.findBookById(bookId);
        // Use the mapper to convert it to a public DTO
        BookOutput bookOutput = bookMapper.toBookOutput(foundBook);
        // Return the DTO with a 200 OK Status
        return ResponseEntity.ok(bookOutput);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookOutput> updateBook(UUID bookId, BookInput bookInput) {
        // Call the service function
        Book updatedBook = bookService.updateBook(bookId, bookInput);
        // Use the mapper to convert it to a public DTO
        BookOutput bookOutput = bookMapper.toBookOutput(updatedBook);
        // Return the DTO with a 200 OK Status
        return ResponseEntity.ok(bookOutput);
    }
}
