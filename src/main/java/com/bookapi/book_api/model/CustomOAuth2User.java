package com.bookapi.book_api.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final User localUser;

    public CustomOAuth2User(OAuth2User oAuth2User, User localUser) {
        this.oAuth2User = oAuth2User;
        this.localUser = localUser;
    }

    @Override // Standard OAuth2User methods should be handled by the original object
    public Map<String, Object > getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public String getName() {
        // Use the email address as the 'name' for the new Custom User
        return oAuth2User.getAttribute("email");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(localUser.getRole()));
    }

    public User getLocalUser() {
        return localUser;
    }
}
