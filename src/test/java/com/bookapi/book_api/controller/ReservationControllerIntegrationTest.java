package com.bookapi.book_api.controller;


import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.model.CustomOAuth2User;
import com.bookapi.book_api.model.Reservation;
import com.bookapi.book_api.model.User;
import com.bookapi.book_api.repository.BookRepository;
import com.bookapi.book_api.repository.ReservationRepository;
import com.bookapi.book_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        reservationRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /reservations should create a new reservation in the collection and return it with 201 Status")
    void postReservation_whenBookExists_andUserIsAuthenticated_shouldCreateReservation() throws Exception {
        // GIVEN a scenario with a book and an existing user
        var fixture = setUpMultiUserScenario();

        // AND userOne is logged in
        var auth = createAuthenticationFor(fixture.userOne());

        // WHEN a POST request is made to the reservations endpoint
        var resultActions = mockMvc.perform(post("/books/{bookId}/reservations", fixture.book().getId())
                .with(authentication(auth)));

        // THEN the response should be 201 Created
        resultActions.andExpect(status().isCreated())
                // AND the response body should contain the new Reservation document
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.bookId", is(fixture.book().getId().toString())))
                .andExpect(jsonPath("$.userId", is(fixture.userOne().getId().toString())));
    }

    @Test
    @DisplayName("GET /reservations/{id} returns 200 OK and the document for an existing reservation")
    void getReservationById_whenReservationExists_shouldReturnReservationDetails() throws Exception {
        // GIVEN a scenario with a book, an existing user, and a reservation by that user
        var fixture = setUpMultiUserScenario();

        // AND that user is logged in
        var auth = createAuthenticationFor(fixture.userOne());

        // WHEN a GET request is made to the reservation endpoint
        var resultActions = mockMvc.perform(get(
                "/books/{bookId}/reservations/{reservationId}",
                fixture.book().getId(),
                fixture.reservationForUserOne().getId()
                )
                .with(authentication(auth))
        );

        // THEN the response should be 200 OK and contain the reservation in the body
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(fixture.reservationForUserOne().getId().toString())))
                .andExpect(jsonPath("$.bookId", is(fixture.book().getId().toString())))
                .andExpect(jsonPath("$.userId", is(fixture.userOne().getId().toString())));
    }

    @Test
    @DisplayName("GET /reservations/{id} returns 403 Forbidden if user is not the owner")
    void getReservationById_whenUserIsNotOwner_shouldReturn403() throws Exception {
        // GIVEN a scenario with a book, 2 existing users and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND userTwo (who does not own the reservation) is logged in
        var auth = createAuthenticationFor(fixture.userTwo());

        // WHEN a GET request is made to the reservation endpoint by the non-owner user
        var resultActions = mockMvc.perform(get(
                        "/books/{bookId}/reservations/{reservationId}",
                        fixture.book().getId(),
                        fixture.reservationForUserOne().getId()
                        )
                        .with(authentication(auth))
        );

        // THEN the result should be 403 Forbidden
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test_user", roles = {"USER"})
    @DisplayName("DELETE /reservations/{id} should delete the book and return 204 No Content")
    void deleteReservation_whenReservationExists_shouldDeleteBookAndReturn204() throws Exception {
        // GIVEN a scenario with a book, an existing user, and a reservation by that user
        var fixture = setUpMultiUserScenario();

        // AND that user is logged in
        var auth = createAuthenticationFor(fixture.userOne());

        // WHEN a DELETE request is made to the reservation endpoint
        var resultActions = mockMvc.perform(delete(
                "/books/{bookId}/reservations/{reservationId}",
                fixture.book().getId(),
                fixture.reservationForUserOne().getId()
                )
                .with(authentication(auth)));

        // THEN the response status should be 204 No Content and the body empty
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /reservations/{id} returns 403 Forbidden if user is not the owner")
    void deleteReservationById_whenUserIsNotOwner_shouldReturn403() throws Exception {
        // GIVEN a scenario with a book, 2 existing users and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND userTwo (who does not own the reservation) is logged in
        var auth = createAuthenticationFor(fixture.userTwo());

        // WHEN a GET request is made to the reservation endpoint by the non-owner user
        var resultActions = mockMvc.perform(delete(
                        "/books/{bookId}/reservations/{reservationId}",
                        fixture.book().getId(),
                        fixture.reservationForUserOne().getId()
                )
                        .with(authentication(auth))
        );

        // THEN the result should be 403 Forbidden
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user_one")
    @DisplayName("GET /reservations should return only the authenticated user's reservations- ]")
    void listReservations_whenCalledByUser_shouldReturnOnlyTheirReservations() throws Exception {
        // GIVEN a book exists in the database
        Book existingBook = new Book("A Book to Delete", "An Author", "Its Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // AND two existing users in the database
        User userOne = new User("one@example.com", "User One", "ROLE_USER");
        userRepository.save(userOne);
        User userTwo = new User("two@example.com", "User Two", "ROLE_USER");
        userRepository.save(userTwo);

        // AND each user has a reservation
        reservationRepository.save(new Reservation(bookId, userOne.getId()));
        reservationRepository.save(new Reservation(bookId, userTwo.getId()));

        // AND user one is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), userOne);
        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        // WHEN a GET request is made to the /reservations endpoint
        var resultActions = mockMvc.perform(get("/reservations")
                .with(authentication(auth))
        );

        // THEN the response is 200 OK and is a list only containing the reservation for user one
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].userId", is(userOne.getId().toString())));
    }

    private record TestUsersAndReservation(
            User userOne,
            User userTwo,
            Book book,
            Reservation reservationForUserOne
    ) {}

    /**
     * Helper method to create a standard test scenario with:
     * 2 distinct users,
     * a book,
     * a reservation that belongs to the 1st user.
     *  This provides a consistent 'world' for authentication tests.
     * @return A {@link TestUsersAndReservation} record containing all the created entities.
     */
    private TestUsersAndReservation setUpMultiUserScenario() {
        Book book = new Book("A Test Book", "Test Author", "Test Synopsis");
        bookRepository.save(book);

        User userOne = new User("userone@example.com", "User One", "ROLE_USER");
        userRepository.save(userOne);

        User userTwo = new User("usertwo@example.com", "User Two", "ROLE_USER");
        userRepository.save(userTwo);

        // The Reservation created will belong to User One
        Reservation reservation = new Reservation(book.getId(), userOne.getId());
        reservationRepository.save(reservation);

        return new TestUsersAndReservation(userOne, userTwo, book, reservation);
    }

    /**
     * Helper method to build a mock {@link Authentication} object for a given {@link User}.
     * This is used with MockMvc's {@code .with(authentication(...))} to simulate
     * a request from a specific authenticated user.
     *
     * @param user The User entity to create the principal from.
     * @return A fully formed Authentication object for use in tests.
     */
    private Authentication createAuthenticationFor(User user) {
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), user);
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
    }
}
