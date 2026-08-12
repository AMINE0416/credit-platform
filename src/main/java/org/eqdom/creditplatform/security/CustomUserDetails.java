package org.eqdom.creditplatform.security;

import org.eqdom.creditplatform.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) { this.user = user; }

    public User getUser() { return user; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Protection contre les rôles null
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getNom()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return user.isActif(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.isActif(); }
}