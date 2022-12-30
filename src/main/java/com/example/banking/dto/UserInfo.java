package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserInfo {
    private String fullName;
    private String registrationDate;
    private String phoneNumber;
}