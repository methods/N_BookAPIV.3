// src/test/java/com/bookapi/bookapi/controller/BookControllerIntegrationTest.java
package com.bookapi.book_api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // (1)
@AutoConfigureMockMvc // (2)
public class BookControllerIntegrationTest {

    @Autowired // (3)
    private MockMvc mockMvc;

    @Test // (4)
    @WithMockUser
    void getBookById_whenEndpointExists_shouldReturn501NotImplemented() throws Exception {

        // Arrange
        UUID bookId = UUID.randomUUID();

        // Act & Assert
        mockMvc.perform(get("/books/{bookId}", bookId)) // (5)
                .andExpect(status().isNotImplemented()); // (6)
    }
}