package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.stream.Collectors;

public class DefaultUserDetails implements UserDetails {
    private final Account account;

    public DefaultUserDetails(Account account) {
        super();
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.commaSeparatedStringToAuthorityList(
            StringUtils.collectionToCommaDelimitedString(
                this.account.getRoles().stream()
                    .map(Role::toString)
                    .collect(Collectors.toList())
            )
        );
    }

    @Override
    public String getPassword() {
        return this.account.getEncodedPassword();
    }

    @Override
    public String getUsername() {
        return this.account.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Account getUser() {
        return this.account;
    }
}
