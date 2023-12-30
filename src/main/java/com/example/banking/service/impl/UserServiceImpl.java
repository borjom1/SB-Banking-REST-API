package com.example.banking.service.impl;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.dto.UserInfo;
import com.example.banking.entity.RoleEntity;
import com.example.banking.entity.RoleEntity.Roles;
import com.example.banking.entity.UserEntity;
import com.example.banking.exception.UserNotFoundException;
import com.example.banking.exception.RoleNotFoundException;
import com.example.banking.model.TokenPair;
import com.example.banking.repository.RoleRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.jwt.JWTProvider;
import com.example.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.banking.security.jwt.TokenType.REFRESH;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JWTProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public TokenPair login(LoginRequest request) {
        log.debug("-> verifyUser()");
        UserEntity user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password is not correct");
        }

        // generate tokens & retrieve their expiration
        TokenPair tokens = jwtProvider.generateTokenPair(user);

        // update refresh token
        user.setRefreshToken(tokens.refresh());

        log.debug("-> verifyUser(): {} logged in", request.getPhoneNumber());
        return tokens;
    }

    @Override
    public void register(RegisterRequest request) {
        log.debug("-> register()");

        Optional<UserEntity> user = userRepository.findByPhoneNumber(request.getPhoneNumber());
        if (user.isPresent()) {
            throw new BadCredentialsException("User with such phone-number already exists");
        }

        user = userRepository.findByIpn(request.getIpn());
        if (user.isPresent()) {
            throw new BadCredentialsException("User with such ipn already exists");
        }

        RoleEntity userRole = roleRepository.findByName(Roles.USER.getRoleName())
                .orElseThrow(() -> new RoleNotFoundException("Role \"USER\" not found"));

        UserEntity newUser = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .ipn(request.getIpn())
                .password(passwordEncoder.encode(request.getPassword()))
                .registeredAt(ZonedDateTime.now())
                .build();

        newUser.addRole(userRole);

        userRepository.save(newUser);
        log.debug("-> register(): {} registered", newUser.getPhoneNumber());
    }

    @Transactional
    @Override
    public void logout(String refreshToken) {
        UserEntity user = verifyRefreshToken(refreshToken);
        user.setRefreshToken(null);
        log.debug("-> logout(): {} successfully logged out", user.getPhoneNumber());
    }

    @Override
    public UserEntity findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("user with specified identifier not found"));
    }

    @Transactional
    @Override
    public TokenPair refreshTokens(String refreshToken) {
        log.debug("-> getTokens()");
        UserEntity user = verifyRefreshToken(refreshToken);

        // if refresh tokens match then generate a new pair of tokens and return
        TokenPair tokens = jwtProvider.generateTokenPair(user);
        user.setRefreshToken(tokens.refresh());

        log.debug("-> getTokens(): {} refreshed tokens", user.getPhoneNumber());
        return tokens;
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserEntity user = findUser(userId);

        return new UserInfo(
                user.getFirstName() + " " + user.getLastName(),
                user.getRegisteredAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                user.getPhoneNumber()
        );
    }

    private UserEntity verifyRefreshToken(String refreshToken) {

        Long userId = ofNullable(jwtProvider.getUserId(refreshToken, REFRESH))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User was not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new BadCredentialsException("Refresh token is fake");
        }
        return user;
    }

}