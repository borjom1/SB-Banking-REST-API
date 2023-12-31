package com.example.banking.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class UserInfo {
    private String fullName;
    private String registrationDate;
    private String phoneNumber;
}