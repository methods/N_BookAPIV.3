// src/test/java/com/bookapi/bookapi/controller/BookControllerIntegrationTest.java
package com.bookapi.book_api.controller;

import com.bookapi.book_api.dto.generated.BookInput;
import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // (1)
@AutoConfigureMockMvc // (2)
public class BookControllerIntegrationTest {

    @Autowired // (3)
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser
    @DisplayName(("POST /books should create a new book in the database and return 201"))
    void postBook_whenBookIsValid_shouldCreateBookAndReturn201() throws Exception {
        // GIVEN a DTO representing the request body
        BookInput bookInput = new BookInput();
        bookInput.setTitle("Fahrenheit 451");
        bookInput.setAuthor("Ray Bradbury");
        bookInput.setSynopsis("A dystopian novel about a future society that burns books.");
        // Convert the DTO to JSON to represent an incoming request
        String bookInputJson = objectMapper.writeValueAsString(bookInput);

        // WHEN a POST request is made with the appropriate request body
        var resultActions = mockMvc.perform(post("/books")
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookInputJson));

        // THEN the response should be 201 OK and contain the new book document
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title", is("Fahrenheit 451")))
                .andExpect(jsonPath("$.author", is("Ray Bradbury")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /books/{id} returns 200 OK with book details for an existing book")
    void getBookById_whenBookExists_shouldReturnBookDetails() throws Exception {
        // GIVEN a book exists in the database
        Book testBook = new Book("The Hobbit", "J.R.R.Tolkien", "An Unexpected Journey");
        bookRepository.save(testBook);

        // WHEN a request is made for that book's ID
        var resultActions = mockMvc.perform(get("/books/{bookId}", testBook.getId()));

        // THEN the response is successful and contains the correct data
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("The Hobbit")))
                .andExpect(jsonPath("$.author", is("J.R.R.Tolkien")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /books/{id} returns 404 Not Found for a non-existent book")
    void getBookById_whenBookDoesNotExist_shouldReturn404NotFound() throws Exception {
        // GIVEN a book ID that does not exist in the database
        UUID nonExistentId = UUID.randomUUID();

        // WHEN a GET request is made for that book's ID
        var resultActions = mockMvc.perform(get("/books/{bookId}", nonExistentId));

        // THEN the response status is 404 Not Found and contains an error message
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Book not found with id: " + nonExistentId)));
    }
}