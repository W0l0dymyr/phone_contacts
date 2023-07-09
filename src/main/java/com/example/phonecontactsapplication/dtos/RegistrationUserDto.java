package com.example.phonecontactsapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
    @AllArgsConstructor
@RequiredArgsConstructor
    public class RegistrationUserDto {
        private String login;
        private String password;

}
