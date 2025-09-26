package com.bookapi.book_api.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OidcUser {

    private final OidcUser oidcUser;
    private final User localUser;

    public CustomOAuth2User(OidcUser oidcUser, User localUser) {
        this.oidcUser = oidcUser;
        this.localUser = localUser;
    }

    @Override // Standard OAuth2User methods should be handled by the original object
    public Map<String, Object > getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public String getName() {
        // Use the email address as the 'name' for the new Custom User
        return oidcUser.getAttribute("email");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(localUser.getRole()));
    }

    public User getLocalUser() {
        return localUser;
    }

    @Override
    public Map<String, Object> getClaims() {
        return Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }
}
