// src/test/java/com/bookapi/bookapi/controller/BookControllerIntegrationTest.java
package com.bookapi.book_api.controller;

import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // (1)
@AutoConfigureMockMvc // (2)
public class BookControllerIntegrationTest {

    @Autowired // (3)
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test // (4)
    @WithMockUser
    void getBookById_whenEndpointExists_shouldReturn501NotImplemented() throws Exception {

        // Arrange
        UUID bookId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/books/{bookId}", bookId)) // (5)
                .andExpect(status().isNotImplemented()); // (6)
    }

    @Test
    @WithMockUser
    void getBookById_whenBookExists_shouldReturnBookDetails() throws Exception {
        // Arrange
        // Create a book object, save it to the database
        Book testBook = new Book("The Hobbit", "J.R.R.Tolkien", "An Unexpected Journey");
        bookRepository.save(testBook);

        // Act
        // Make a GET request to the endpoint using the new book's id
        mockMvc.perform(get("/books/{bookId}", testBook.getId()))
                // Assert
                // Expect HTTP 200 OK Status code
                .andExpect(status().isOk())
                // Expect the response JSON to contain the supplied title and author
                .andExpect(jsonPath("$.title", is("The Hobbit")))
                .andExpect(jsonPath("$.author", is("J.R.R.Tolkien")));

    }
}