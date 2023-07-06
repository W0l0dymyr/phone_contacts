package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.dtos.JwtRequest;
import com.example.phonecontactsapplication.dtos.RegistrationUserDto;
import com.example.phonecontactsapplication.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController{
    @Autowired
    private AuthService authService;

    @PostMapping("/auth")
    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        return authService.createAuthToken(authRequest);
    }

    @PostMapping("/registration")
    public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
        return authService.createNewUser(registrationUserDto);
    }
    @GetMapping("/")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("Hello");
    }

}