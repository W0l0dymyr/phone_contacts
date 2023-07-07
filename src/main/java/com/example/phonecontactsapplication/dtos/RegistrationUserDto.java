package com.example.phonecontactsapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

    @Data
    @AllArgsConstructor
    public class RegistrationUserDto {
        private String login;
        private String password;

}
