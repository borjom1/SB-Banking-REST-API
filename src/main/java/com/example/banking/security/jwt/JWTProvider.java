package com.example.banking.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.banking.entity.UserEntity;
import com.example.banking.model.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.example.banking.security.jwt.TokenType.ACCESS;
import static com.example.banking.security.jwt.TokenType.REFRESH;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTProvider {

    @Value("${jwt.secret.access}")
    private String accessSecret;

    @Value("${jwt.secret.refresh}")
    private String refreshSecret;

    @Value("${jwt.subject}")
    private String subject;

    @Value("${jwt.issuer}")
    private String issuer;

    private static final int ACCESS_MINUTES_VALIDITY = 30;

    private static final int REFRESH_DAYS_VALIDITY = 30;

    private final UserDetailsService userDetailsService;

    public boolean isTokenValid(String token, TokenType type) {
        try {
            getDecodedJWT(token, type);
            return true;
        } catch (JWTVerificationException e) {
            log.error("{}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String phoneNumber = retrieveClaims(token, TokenType.ACCESS).get("phoneNumber").asString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String generateToken(TokenType type, String phoneNumber, Long id) {
        Instant now = ZonedDateTime.now().toInstant();

        Instant expiration = type == TokenType.REFRESH ?
                now.plus(REFRESH_DAYS_VALIDITY, ChronoUnit.DAYS) :
                now.plus(ACCESS_MINUTES_VALIDITY, ChronoUnit.MINUTES);

        return JWT.create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withClaim("phoneNumber", phoneNumber)
                .withClaim("id", id)
                .withExpiresAt(Date.from(expiration)
                ).sign(Algorithm.HMAC256(type == TokenType.REFRESH ? refreshSecret : accessSecret));
    }

    public String getToken(@NonNull HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return bearer == null || bearer.length() < 7 ? null : bearer.substring(7);
    }

    private DecodedJWT getDecodedJWT(String token, TokenType type) {
        return JWT.require(Algorithm.HMAC256(type == TokenType.REFRESH ? refreshSecret : accessSecret))
                .withSubject(subject)
                .withIssuer(issuer)
                .build()
                .verify(token);
    }

    private Map<String, Claim> retrieveClaims(String token, TokenType type) {
        return getDecodedJWT(token, type).getClaims();
    }

    public Long getUserId(String token, TokenType type) {
        try {
            return retrieveClaims(token, type).get("id").asLong();
        } catch (JWTVerificationException e) {
            log.info("IN JwtProvider -> getUserId(): exception {}", e.getMessage());
            return null;
        }
    }

    public TokenPair generateTokenPair(@NonNull UserEntity user) {
        String accessToken = generateToken(ACCESS, user.getPhoneNumber(), user.getId());
        String refreshToken = generateToken(REFRESH, user.getPhoneNumber(), user.getId());
        return new TokenPair(accessToken, refreshToken);
    }

}