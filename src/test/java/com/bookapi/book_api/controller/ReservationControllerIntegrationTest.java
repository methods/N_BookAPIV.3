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
    @DisplayName("GET /reservations should return only the authenticated user's reservations- ]")
    void listReservations_whenCalledByUser_shouldReturnOnlyTheirReservations() throws Exception {
        // GIVEN a scenario with a book, two existing users, and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND userTwo also has a reservation for the book
        reservationRepository.save(new Reservation(
                fixture.book().getId(),
                fixture.userTwo().getId()));

        // AND userOne is logged in
        var auth = createAuthenticationFor(fixture.userOne());

        // WHEN a GET request is made to the /reservations endpoint
        var resultActions = mockMvc.perform(get("/reservations")
                .with(authentication(auth))
        );

        // THEN the response is 200 OK and is a list only containing the reservation for userOne
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].userId", is(fixture.userOne().getId().toString())));
    }

    @Test
    @DisplayName("GET /reservations?userId={userId} for an admin user should return the list of the user's reservations")
    void listReservations_whenCalledByAdmin_shouldReturnQueriedUsersReservations() throws Exception {
        // GIVEN a scenario with a book, two existing users, and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND userTwo also has a reservation for the book
        reservationRepository.save(new Reservation(
                fixture.book().getId(),
                fixture.userTwo().getId()));

        // AND an admin user
        User adminUser = new User("admin@example.com", "Admin User", "ROLE_ADMIN");
        userRepository.save(adminUser);
        // AND the admin user is logged in
        var auth = createAuthenticationFor(adminUser);

        // WHEN a GET request is made to the /reservations?userId={userOne} endpoint
        var resultActions = mockMvc.perform(get("/reservations")
                        .param("userId", fixture.userOne().getId().toString())
                        .with(authentication(auth))
        );

        // THEN the response is 200 OK and is a list only containing the reservation for userOne
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()", is(1)))
                .andExpect(jsonPath("$.items[0].userId", is(fixture.userOne().getId().toString())));
    }

    @Test
    @DisplayName("GET /reservations as an admin with no userId should return all reservations")
    void listReservations_whenCalledByAdminWithoutUserId_shouldReturnAllReservations() throws Exception {
        // GIVEN a scenario with a book, two existing users, and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND userTwo also has a reservation for the book
        reservationRepository.save(new Reservation(
                fixture.book().getId(),
                fixture.userTwo().getId()));

        // AND an admin user
        User adminUser = new User("admin@example.com", "Admin User", "ROLE_ADMIN");
        userRepository.save(adminUser);
        // AND the admin user is logged in
        var auth = createAuthenticationFor(adminUser);

        // WHEN a GET request is made to the /reservations endpoint
        var resultActions = mockMvc.perform(get("/reservations")
                .with(authentication(auth))
        );

        // THEN the response is 200 OK and is a list containing all reservations
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount", is(2)))
                .andExpect(jsonPath("$.items.length()", is(2)));
    }

    @Test
    @DisplayName("GET /reservations as an admin should support pagination")
    void listReservations_whenCalledByAdmin_shouldBePaginated() throws Exception {
        // GIVEN a scenario with a book, two existing users, and a reservation by userOne
        var fixture = setUpMultiUserScenario();

        // AND 2 more reservations
        reservationRepository.save(new Reservation(fixture.book().getId(), fixture.userTwo().getId()));
        reservationRepository.save(new Reservation(fixture.book().getId(), fixture.userOne().getId()));

        // AND an admin user
        User adminUser = new User("admin@example.com", "Admin User", "ROLE_ADMIN");
        userRepository.save(adminUser);
        // AND the admin user is logged in
        var adminAuth = createAuthenticationFor(adminUser);

        // WHEN the admin requests the second page with a size of 2
        var resultActions = mockMvc.perform(get("/reservations")
                .param("offset", "2")
                .param("limit", "2")
                .with(authentication(adminAuth))
        );

        // THEN the response is 200 OK and contains the correct paginated data
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount", is(3)))
                .andExpect(jsonPath("$.items.length()", is(1))); // The second page of size 2 should have only 1 item
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
