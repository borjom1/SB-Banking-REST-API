package com.example.banking.service.impl;

import com.example.banking.dto.RegisterRequest;
import com.example.banking.entity.RoleEntity;
import com.example.banking.entity.UserEntity;
import com.example.banking.repository.RoleRepository;
import com.example.banking.repository.UserRepository;
import com.example.banking.security.jwt.JWTProvider;
import com.example.banking.security.jwt.JWTType;
import com.example.banking.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final JWTProvider jwtProvider;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           JWTProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Map<String, String> verifyUser(String phoneNumber, String password) {
        log.info("IN UserServiceImpl -> verifyUser()");
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User with such phone-number does not exist"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Password is not correct");
        }

        // generate tokens & retrieve their expiration
        Map<String, String> result = generatesTokens(user);

        // save refresh token
        user.setRefreshToken(result.get("refreshToken"));
        userRepository.save(user);

        log.info("IN UserServiceImpl -> verifyUser(): {} user verified", phoneNumber);
        return result;
    }

    @Override
    public void register(RegisterRequest request) {
        log.info("IN UserServiceImpl -> register()");
        UserEntity user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        if (user != null) {
            throw new BadCredentialsException("User with such phone-number already exists");
        }
        user = userRepository.findByIpn(request.getIpn()).orElse(null);
        if (user != null) {
            throw new BadCredentialsException("User with such ipn already exists");
        }

        // find & give role to new user
        Set<RoleEntity> roles = new HashSet<>();
        roleRepository.findByName("ROLE_USER").ifPresent(roles::add);

        user = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .ipn(request.getIpn())
                .password(passwordEncoder.encode(request.getPassword()))
                .registeredAt(ZonedDateTime.now())
                .roles(roles)
                .build();

        userRepository.save(user);
        log.info("IN register(): {} user was saved", user.getPhoneNumber());
    }

    @Override
    public void logout(String refreshToken) {
        UserEntity user = verifyRefreshToken(refreshToken);
        user.setRefreshToken(null);
        userRepository.save(user);
        log.info("IN UserServiceImpl -> logout(): {} user successfully logged out", user.getPhoneNumber());
    }

    @Override
    public Map<String, String> getTokens(String refreshToken) {
        log.info("IN UserServiceImpl -> getTokens()");
        UserEntity user = verifyRefreshToken(refreshToken);

        // if refresh tokens match then generate a new pair of tokens and return
        Map<String, String> result = generatesTokens(user);

        // save refresh token
        user.setRefreshToken(result.get("refreshToken"));
        userRepository.save(user);

        log.info("IN UserServiceImpl -> getTokens(): {} user refreshed success", user.getPhoneNumber());
        return result;
    }

    private UserEntity verifyRefreshToken(String refreshToken) {
        // validating refresh token
        Integer userId = Optional.ofNullable(jwtProvider.getUserId(refreshToken, JWTType.REFRESH))
                .orElseThrow(() -> new BadCredentialsException("Invalid Refresh JWT"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("User was not found"));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new BadCredentialsException("Refresh JWT is fake");
        }
        return user;
    }

    private Map<String, String> generatesTokens(UserEntity user) {
        String accessToken = jwtProvider.generateToken(JWTType.ACCESS, user.getPhoneNumber(), user.getId());
        ZonedDateTime accessExpiration = jwtProvider.getExpirationDate(accessToken, JWTType.ACCESS);

        String refreshToken = jwtProvider.generateToken(JWTType.REFRESH, user.getPhoneNumber(), user.getId());
        ZonedDateTime refreshExpiration = jwtProvider.getExpirationDate(refreshToken, JWTType.REFRESH);

        // wrap info
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("accessTokenExpiration", accessExpiration.toString());
        result.put("refreshToken", refreshToken);
        result.put("refreshTokenExpiration", refreshExpiration.toString());

        return result;
    }

}