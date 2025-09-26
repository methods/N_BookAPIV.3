package com.bookapi.book_api.service;

import com.bookapi.book_api.model.CustomOAuth2User;
import com.bookapi.book_api.model.User;
import com.bookapi.book_api.repository.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOidcUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Use Spring's built-in class to get the OIDC user data
        OidcUser oidcUser = super.loadUser(userRequest);

        // Use our protected internal service function
        User user = processUserRegistration(oidcUser);

        return new CustomOAuth2User(oidcUser, user);
    }

    protected User processUserRegistration(OidcUser oidcUser) {
        // Get the attributes from the OidcUser object
        String email = oidcUser.getEmail();
        String name = oidcUser.getName();
        System.out.println("Email from OIDC: " + email + ", Name: " + name);

        // Find the user in the database or create a new user document
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            System.out.println("User found in DB. Updating...");
            user = userOptional.get();
            user.setName(name);
        } else {
            System.out.println("User not found in DB. Creating new user...");
            user = new User(email, name, "ROLE_USER");
        }

        // Update the db
        userRepository.save(user);
        return user;
    }
}
