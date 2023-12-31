package com.example.banking.security.jwt;

import com.example.banking.entity.RoleEntity;
import com.example.banking.entity.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
public class JWTUserDetails implements UserDetails {

    private final Long id;
    private final String phoneNumber;
    private final String password;
    private final Set<RoleEntity> roles;

    private JWTUserDetails(Long id, String phoneNumber, String password, Set<RoleEntity> roles) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.roles = roles;
    }

    public static UserDetails of(UserEntity user) {
        return new JWTUserDetails(
                user.getId(),
                user.getPhoneNumber(),
                user.getPassword(),
                user.getRoles()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}