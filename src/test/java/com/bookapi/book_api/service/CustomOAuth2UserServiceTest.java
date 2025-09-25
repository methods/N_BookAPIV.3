package com.bookapi.book_api.service;


import com.bookapi.book_api.model.User;
import com.bookapi.book_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    @Mock // Creates a mock (fake) UserRepository
    private UserRepository userRepository;

    @InjectMocks // Creates an instance of our service and injects the mock repository into it
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock // Mock the inputs to the user service
    private OAuth2UserRequest userRequest;

    @Mock
    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp () {
        MockitoAnnotations.openMocks(this); // Initialize all the Mock and InjectMocks annotations
    }

    @Test
    @DisplayName("A new user login via OAuth should create and save a new user in the database")
    void processUserRegistration_whenUserIsNew_createsAndSavesUser() {
        // GIVEN a user with no existing database entry
        String email = "new.user@example.com";
        String name = "Not Indb";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // AND their details are returned by the OAuth2 provider
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);

        // WHEN the processUserRegistration method is called
        customOAuth2UserService.processUserRegistration(oAuth2User);

        // THEN the user should be saved to the database with their details and role set as USER
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getName()).isEqualTo(name);
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("An existing user login via OAuth should update their name and save the user")
    void processUserRegistration_whenUserExists_updatesAndSavesUser() {
        // GIVEN a user already exists in the database
        String email = "existing.user@example.com";
        User existingUserInDb = new User(email, "Old Name", "ROLE_ADMIN");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUserInDb));

        // AND their details are returned by the OAuth2 provider with a new name
        String newNameFromGoogle = "New Name From Google";
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(newNameFromGoogle);

        // WHEN the processUserRegistration method is called
        customOAuth2UserService.processUserRegistration(oAuth2User);

        // THEN the existing user object should be saved with the updated name
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.getId()).isEqualTo(existingUserInDb.getId());
        assertThat(savedUser.getName()).isEqualTo(newNameFromGoogle);
        assertThat(savedUser.getRole()).isEqualTo("ROLE_ADMIN");
    }
}
