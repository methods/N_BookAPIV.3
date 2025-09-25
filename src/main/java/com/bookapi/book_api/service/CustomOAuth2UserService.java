package com.bookapi.book_api.service;

import com.bookapi.book_api.model.CustomOAuth2User;
import com.bookapi.book_api.model.User;
import com.bookapi.book_api.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Use Spring's build in oauth service to fetch the user details from the provider
        OAuth2User oAuth2User = super.loadUser(userRequest);
        User user = processUserRegistration(oAuth2User);

        // Return the user information
        return new CustomOAuth2User(oAuth2User, user);
    }

    protected User processUserRegistration(OAuth2User oAuth2User) {
        // Extract the attributes
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Find the user in the database, or create a new user document
        // .findByEmail returns an Optional<User> object
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(name);
        } else {
            user = new User(email, name, "ROLE_USER");
        }

        // Save the new or updated user to the database
        userRepository.save(user);

        return user;
    }
}

