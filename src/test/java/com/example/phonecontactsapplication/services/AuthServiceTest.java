package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.dtos.JwtRequest;
import com.example.phonecontactsapplication.dtos.JwtResponse;
import com.example.phonecontactsapplication.dtos.RegistrationUserDto;
import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.utils.JwtTokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private JwtTokenUtils jwtTokenUtils;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private  UserDetails userDetails;

    @InjectMocks
    private AuthService authService;
    @Test
    public void testCreateAuthToken_ValidCredentials() {
        // Arrange
        JwtRequest authRequest = new JwtRequest("john", "password");
        String token = "jwt-token";

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        Mockito.when(userService.loadUserByUsername(authRequest.getLogin())).thenReturn(userDetails);
        Mockito.when(jwtTokenUtils.generateToken(userDetails)).thenReturn(token);

        // Act
        ResponseEntity<?> response = authService.createAuthToken(authRequest);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(new JwtResponse(token), response.getBody());
    }

    @Test
    public void testCreateAuthToken_InvalidCredentials() {
        // Arrange
        JwtRequest authRequest = new JwtRequest("john", "invalid-password");

        Mockito.when(authenticationManager.authenticate(Mockito.any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // Act
        ResponseEntity<?> response = authService.createAuthToken(authRequest);

        // Assert
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Assertions.assertEquals("Bad credentials", response.getBody());
    }

    @Test
    public void testCreateNewUser_ValidUser() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("john", "password");
        User user = new User();
        user.setLogin(registrationUserDto.getLogin());
        Mockito.when(userService.findByLogin(registrationUserDto.getLogin())).thenReturn(Optional.empty());
        Mockito.when(userService.createNewUser(registrationUserDto)).thenReturn(user);

        // Act
        ResponseEntity<?> response = authService.createNewUser(registrationUserDto);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("User john is successfully registered", response.getBody());
    }

    @Test
    public void testCreateNewUser_LoginAlreadyExists() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("john", "password");
        Mockito.when(userService.findByLogin(registrationUserDto.getLogin())).thenReturn(Optional.of(new User()));

        // Act
        ResponseEntity<?> response = authService.createNewUser(registrationUserDto);

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Login already exists", response.getBody());
    }

    @Test
    public void testCreateNewUser_EmptyLogin() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("", "password");

        // Act
        ResponseEntity<?> response = authService.createNewUser(registrationUserDto);

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Login cannot be empty", response.getBody());
    }

    @Test
    public void testCreateNewUser_EmptyPassword() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto("john", "");

        // Act
        ResponseEntity<?> response = authService.createNewUser(registrationUserDto);

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Password cannot be empty", response.getBody());
    }

}
