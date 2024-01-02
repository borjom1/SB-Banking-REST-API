package com.example.banking.security.jwt;

import com.example.banking.dto.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZonedDateTime;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.debug("IN JwtFilter -> doFilterInternal()");

        String accessToken = jwtProvider.getToken(request);
        if (accessToken != null) {
            if (!jwtProvider.isTokenValid(accessToken, TokenType.ACCESS)) {
                log.debug("Invalid token");
                handleUnauthorizedRequest(request, response);
                return;
            } else {
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
    private void handleUnauthorizedRequest(@NonNull HttpServletRequest request,
                                           @NonNull HttpServletResponse response) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(SC_UNAUTHORIZED);

        ApiError apiError = new ApiError(
                request.getRequestURI(),
                ZonedDateTime.now(),
                SC_UNAUTHORIZED,
                "Invalid access token"
        );

        @Cleanup ServletOutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, apiError);
    }
}