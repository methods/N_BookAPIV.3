// src/test/java/com/bookapi/bookapi/controller/BookControllerIntegrationTest.java
package com.bookapi.book_api.controller;

import com.bookapi.book_api.dto.generated.BookInput;
import com.bookapi.book_api.model.Book;
import com.bookapi.book_api.model.CustomOAuth2User;
import com.bookapi.book_api.model.User;
import com.bookapi.book_api.repository.BookRepository;
import com.bookapi.book_api.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName(("POST /books for a logged in Admin user should create a new book in the database and return 201"))
    void postBook_whenBookIsValid_andUserIsAdmin_shouldCreateBookAndReturn201() throws Exception {
        // GIVEN an admin user
        User adminUser = new User("user@example.com", "Test User", "ROLE_ADMIN");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), adminUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // AND a DTO representing the request body
        BookInput bookInput = new BookInput();
        bookInput.setTitle("Fahrenheit 451");
        bookInput.setAuthor("Ray Bradbury");
        bookInput.setSynopsis("A dystopian novel about a future society that burns books.");
        String bookInputJson = objectMapper.writeValueAsString(bookInput);

        // WHEN a POST request is made with the appropriate request body
        var resultActions = mockMvc.perform(post("/books")
                        .with(authentication(auth))
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
    @DisplayName("POST /books should return 403 Forbidden for non-admin user")
    void postBook_whenUserIsNotAdmin_shouldReturn403() throws Exception {
        // GIVEN a non-admin user
        User regularUser = new User("user@example.com", "Test User", "ROLE_USER");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), regularUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // AND a DTO representing the request body
        BookInput bookInput = new BookInput();
        bookInput.setTitle("Fahrenheit 451");
        bookInput.setAuthor("Ray Bradbury");
        bookInput.setSynopsis("A dystopian novel about a future society that burns books.");
        String bookInputJson = objectMapper.writeValueAsString(bookInput);

        // WHEN a POST request is made with the appropriate request body
        var resultActions = mockMvc.perform(post("/books")
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(bookInputJson));

        // THEN the response should be 403 Forbidden
        resultActions.andExpect(status().isForbidden());
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

    @Test
    @WithMockUser
    @DisplayName("PUT /books/{id} for a logged in Admin user should update an existing book and return 200 OK")
    void putBook_whenBookExists_andUserIsAdmin_shouldUpdateBookAndReturn200() throws Exception {
        // GIVEN a book that exists in the database
        Book existingBook = new Book("Original Title", "Original Author", "Original Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // AND an admin user
        User adminUser = new User("user@example.com", "Test User", "ROLE_ADMIN");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), adminUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // AND a DTO with the updated details
        BookInput updatedBookInput = new BookInput();
        updatedBookInput.setTitle("Updated Title");
        updatedBookInput.setAuthor("Updated Author");
        updatedBookInput.setSynopsis("Updated Synopsis");
        String updatedBookJson = objectMapper.writeValueAsString(updatedBookInput);

        // WHEN a PUT request is made to that book's ID with the new details
        var resultActions = mockMvc.perform(put("/books/{bookId}", bookId)
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBookJson));

        // THEN the response status should be  200 OK
        resultActions.andExpect(status().isOk())
                // AND the response body should contain the updated book details
                .andExpect(jsonPath("$.id", is(bookId.toString())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.author", is("Updated Author")));
    }

    @Test
    @DisplayName("PUT /books/{id} should return 403 Forbidden for a non-admin user")
    void putBook_whenBookExists_andUserIsNotAdmin_shouldReturn403() throws Exception {
        // GIVEN a non-admin user
        User regularUser = new User("user@example.com", "Test User", "ROLE_USER");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), regularUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // AND a book that exists in the database
        Book existingBook = new Book("Original Title", "Original Author", "Original Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // AND a DTO with the updated details
        BookInput updatedBookInput = new BookInput();
        updatedBookInput.setTitle("Updated Title");
        updatedBookInput.setAuthor("Updated Author");
        updatedBookInput.setSynopsis("Updated Synopsis");
        String updatedBookJson = objectMapper.writeValueAsString(updatedBookInput);

        // WHEN a PUT request is made to that book's ID with the new details
        var resultActions = mockMvc.perform(put("/books/{bookId}", bookId)
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedBookJson));

        // THEN the response should be 403 Forbidden
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /books/{id} for a logged in Admin user should delete the book and return 204 No Content")
    void deleteBook_whenBookExists_shouldDeleteBookAndReturn204() throws Exception {
        // GIVEN a book that exists in the database
        Book existingBook = new Book("To Be Deleted", "Deleted Author", "Deleted Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // AND an admin user
        User adminUser = new User("user@example.com", "Test User", "ROLE_ADMIN");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), adminUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // WHEN a DELETE request is made to that book's ID
        var resultActions = mockMvc.perform(delete("/books/{bookId}", bookId)
                .with(authentication(auth))
                .with(csrf()));

        // THEN the response status should be 204 No Content and the body is empty
        resultActions.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /books/{id} should return 403 Forbidden for a non-admin user")
    void deleteBook_whenBookExists_andUserIsNotAdmin_shouldReturn403() throws Exception {
        // GIVEN a non-admin user
        User regularUser = new User("user@example.com", "Test User", "ROLE_USER");
        // AND that user is logged in
        CustomOAuth2User principal = new CustomOAuth2User(mock(OidcUser.class), regularUser);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // AND a book that exists in the database
        Book existingBook = new Book("To Be Deleted", "Deleted Author", "Deleted Synopsis");
        bookRepository.save(existingBook);
        UUID bookId = existingBook.getId();

        // WHEN a DELETE request is made to that book's ID
        var resultActions = mockMvc.perform(delete("/books/{bookId}", bookId)
                .with(authentication(auth))
                .with(csrf()));

        // THEN the response should be 403 Forbidden
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /books should return a paginated list of books")
    void getAllBooks_whenBooksExist_shouldReturnPaginatedResponse() throws Exception {
        // GIVEN that there are 15 books in the database
        for (int i = 1; i <= 15; i++) {
            bookRepository.save(new Book("Book Title " +i, "Author " +i, "Synopsis " +i));
        }

        // WHEN a request is made for the second page with size 5
        var resultActions = mockMvc.perform(get("/books")
                .param("offset", "5")
                .param("limit", "5"));

        // THEN the response status should be 200 OK and the body should contain the correct page of data
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount", is(15)))
                .andExpect(jsonPath("$.offset", is(5)))
                .andExpect(jsonPath("$.limit", is(5)))
                .andExpect(jsonPath("$.items.length()", is(5)))
                .andExpect(jsonPath("$.items[0].title", is("Book Title 6")));
    }

    @Test
    @DisplayName("Temporary Test: Verify UserRepository can save and find a user")
    void userRepository_shouldSaveAndFindUser() {
        // GIVEN a new user object
        User newUser = new User("test-user@example.com", "Test User", "ROLE_TEST");

        // WHEN we save the user
        userRepository.save(newUser);
        System.out.println("Saved user with ID: " + newUser.getId());

        // AND WHEN we try to find the user by that ID
        Optional<User> foundUserOptional = userRepository.findById(newUser.getId());

        // THEN the user should be found
        assertThat(foundUserOptional).isPresent();

        // AND the found user's email should match
        assertThat(foundUserOptional.get().getEmail()).isEqualTo("test-user@example.com");

        System.out.println("Successfully found user: " + foundUserOptional.get().getEmail());

        // Clean up this specific user
        userRepository.deleteById(newUser.getId());
    }
}