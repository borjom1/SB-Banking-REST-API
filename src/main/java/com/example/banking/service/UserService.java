package com.example.banking.service;

import com.example.banking.dto.auth.LoginRequest;
import com.example.banking.dto.auth.RegisterRequest;
import com.example.banking.dto.UserInfo;
import com.example.banking.entity.UserEntity;
import com.example.banking.model.TokenPair;

public interface UserService {

    void register(RegisterRequest request);
    TokenPair login(LoginRequest request);
    TokenPair refreshTokens(String refreshToken);
    void logout(String refreshToken);

    UserEntity findUser(Long id);
    UserInfo getUserInfo(Long userId);

}