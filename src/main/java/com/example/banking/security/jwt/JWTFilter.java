package com.example.banking.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.debug("IN JwtFilter -> doFilterInternal()");

        String accessToken = jwtProvider.getToken(request);
        if (accessToken != null) {
            if (!jwtProvider.isTokenValid(accessToken, TokenType.ACCESS)) {
                log.warn("Invalid token");
                fillResponse(response);
            } else {
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        log.debug("JWT: {}", accessToken);
        filterChain.doFilter(request, response);
    }
    private void fillResponse(@NonNull HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("error", "Invalid token");
        new ObjectMapper().writeValue(response.getOutputStream(), responseBody);
    }
}