package com.bookapi.book_api.controller;


import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.repository.BookRepository;
import com.bookapi.book_api.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"USER"})
    @DisplayName("POST /reservations should create a new reservation in the collection and return it with 201 Status")
    void postReservation_whenBookExists_andUserIsAuthenticated_shouldCreateReservation() throws Exception {
        // GIVEN a book exists in the database
        Book existingBook = new Book("A Book to Reserve", "An Author", "Its Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // WHEN a POST request is made to the reservations endpoint
        var resultActions = mockMvc.perform(post("/books/{bookId}/reservations", bookId)
                .with(csrf()));

        // THEN the response should be 201 Created
        resultActions.andExpect(status().isCreated())
                // AND the response body should contain the new Reservation document
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.bookId", is(bookId.toString())))
                .andExpect(jsonPath("$.userName", is("test_user")));
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"USER"})
    @DisplayName("GET /reservations/{id} returns 200 OK and the document for an existing reservation")
    void getReservationById_whenReservationExists_shouldReturnReservationDetails() throws Exception {
        // GIVEN a book exists in the database
        Book existingBook = new Book("A Book to Reserve", "An Author", "Its Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // AND it has an existing reservation
        String reservationUsername = "test_user";
        Reservation existingReservation = new Reservation(bookId, reservationUsername);
        reservationRepository.save(existingReservation);
        UUID reservationId = existingReservation.getId();

        // WHEN a GET request is made to the reservation endpoint
        var resultActions = mockMvc.perform(get(
                "/books/{bookId}/reservations/{reservationId}",
                bookId, reservationId
                ));

        // THEN the response should be 200 OK and contain the reservation in the body
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reservationId.toString())))
                .andExpect(jsonPath("$.bookId", is(bookId.toString())))
                .andExpect(jsonPath("$.userName", is(reservationUsername)));
    }
}
