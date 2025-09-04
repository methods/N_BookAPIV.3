package com.bookapi.book_api.controller;

import com.bookapi.book_api.controller.generated.BooksApi;
import com.bookapi.book_api.dto.generated.BookInput;
import com.bookapi.book_api.dto.generated.BookListResponse;
import com.bookapi.book_api.dto.generated.BookOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BookController implements BooksApi {

    @Override
    public ResponseEntity<BookOutput> addBook(BookInput bookInput) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Void> deleteBookById(UUID bookId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<BookListResponse> getAllBooks(Integer offset, Integer limit) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<BookOutput> getBookById(UUID bookId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<BookOutput> updateBook(UUID bookId, BookInput bookInput) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
