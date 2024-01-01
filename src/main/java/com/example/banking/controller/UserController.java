package com.example.banking.controller;

import com.example.banking.dto.UserInfo;
import com.example.banking.security.jwt.JWTUserDetails;
import com.example.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User account management")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Retrieve user info")
    @GetMapping("/info")
    public UserInfo getInfo() {
        var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserInfo(principal.getId());
    }
}