package com.example.banking.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Component
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

    @Autowired
    public JWTProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    public boolean isTokenValid(String token, JWTType type) {
        try {
            var decodedJWT = getDecodedJWT(token, type);
            return true;
        } catch (JWTVerificationException e) {
            log.error("{}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        String phoneNumber = retrieveClaims(token, JWTType.ACCESS).get("phoneNumber").asString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public String generateToken(JWTType type, String phoneNumber, Integer id) {
        Instant now = ZonedDateTime.now().toInstant();

        Instant expiration = type == JWTType.REFRESH ?
                now.plus(REFRESH_DAYS_VALIDITY, ChronoUnit.DAYS) :
                now.plus(ACCESS_MINUTES_VALIDITY, ChronoUnit.MINUTES);

        log.info("{} token exp-time generated: GMT+00:00 {}", type.name(), expiration);
        return JWT.create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withClaim("phoneNumber", phoneNumber)
                .withClaim("id", id)
                .withExpiresAt(Date.from(expiration)
                ).sign(Algorithm.HMAC256(type == JWTType.REFRESH ? refreshSecret : accessSecret));
    }

    public String getToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        return bearer == null ? null : bearer.substring(7);
    }

    private DecodedJWT getDecodedJWT(String token, JWTType type) {
        return JWT.require(Algorithm.HMAC256(type == JWTType.REFRESH ? refreshSecret : accessSecret))
                .withSubject(subject)
                .withIssuer(issuer)
                .build()
                .verify(token);
    }

    private Map<String, Claim> retrieveClaims(String token, JWTType type) {
        return getDecodedJWT(token, type).getClaims();
    }

    public ZonedDateTime getExpirationDate(String token, JWTType type) {
        return ZonedDateTime.ofInstant(
                getDecodedJWT(token, type).getExpiresAt().toInstant(),
                ZoneId.systemDefault()
        );
    }

    public Integer getUserId(String token, JWTType type) {
        try {
            return retrieveClaims(token, type).get("id").asInt();
        } catch (JWTVerificationException e) {
            log.info("IN JwtProvider -> getUserId(): exception {}", e.getMessage());
            return null;
        }
    }

}