package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        User user = new User();
        user.setLogin("aaa");
        user.setPassword(passwordEncoder.encode("aaa"));
        userRepository.save(user);
    }

    @Test
    public void testCreateNewUser() throws Exception {
        // Arrange
        String registrationUserJson = "{\"login\":\"login\", \"password\":\"password\"}";

        // Act & Assert
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationUserJson))
                .andExpect(status().isOk())
                .andExpect(content().string("User login is successfully registered"));

        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login already exists"))
                .andDo(print());
    }

    @Test
    public void testCreateNewUser_LoginIsEmpty() throws Exception {
        // Arrange
        String registrationUserJson = "{\"login\":\"\", \"password\":\"password\"}";

        // Act & Assert
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Login cannot be empty"));
    }

    @Test
    public void testCreateNewUser_PasswordIsEmpty() throws Exception {
        // Arrange
        String registrationUserJson = "{\"login\":\"login\", \"password\":\"\"}";

        // Act & Assert
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password cannot be empty"));
    }

    @Test
    public void testCreateAuthToken_SuccessfulAuthentication() throws Exception {
        // Arrange
        String authRequestJson = "{\"login\":\"aaa\", \"password\":\"aaa\"}";

        // Act & Assert
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("token")));
    }

    @Test
    public void testCreateAuthToken_BadCredentials() throws Exception {
        // Arrange
        String authRequestJson = "{\"login\":\"Stephen\", \"password\":\"aaa\"}";

        // Act & Assert
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }
}