package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.dtos.JwtRequest;
import com.example.phonecontactsapplication.dtos.JwtResponse;
import com.example.phonecontactsapplication.dtos.RegistrationUserDto;
import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.utils.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class AuthService {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    public ResponseEntity<?> createAuthToken(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getLogin(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");
        }
        UserDetails userDetails = userService.loadUserByUsername(authRequest.getLogin());
        String token = jwtTokenUtils.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }
public ResponseEntity<?> createNewUser(@RequestBody RegistrationUserDto registrationUserDto) {
    String login = registrationUserDto.getLogin();
    String password = registrationUserDto.getPassword();

    // Перевірка на порожні значення
    if (login == null || login.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login cannot be empty");
    }
    if (password == null || password.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password cannot be empty");
    }

    if (userService.findByLogin(login).isPresent()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login already exists");
    }

    User user = userService.createNewUser(registrationUserDto);
    return ResponseEntity.ok("User " + user.getLogin() + " is successfully registered");
}

}