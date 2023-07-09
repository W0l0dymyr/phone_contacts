package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.dtos.RegistrationUserDto;
import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.repositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void testLoadUserByUsername_UserFound() {
        // Arrange
        String login = "john";
        String password = "password";
        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        Mockito.when(userRepository.findByLogin(login)).thenReturn(user);

        // Act
        UserDetails userDetails = userService.loadUserByUsername(login);

        // Assert
        Assertions.assertEquals(login, userDetails.getUsername());
        Assertions.assertEquals(password, userDetails.getPassword());
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String login = "john";
        Mockito.when(userRepository.findByLogin(login)).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(login);
        });
    }

    @Test
    public void testCreateNewUser_Success() {
        // Arrange
        RegistrationUserDto registrationUserDto = new RegistrationUserDto();
        registrationUserDto.setLogin("john");
        registrationUserDto.setPassword("password");
        String encodedPassword = "encodedPassword";
        User savedUser = new User();
        savedUser.setLogin(registrationUserDto.getLogin());
        savedUser.setPassword(encodedPassword);
        Mockito.when(passwordEncoder.encode(registrationUserDto.getPassword())).thenReturn(encodedPassword);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(savedUser);

        // Act
        User createdUser = userService.createNewUser(registrationUserDto);

        // Assert
        Assertions.assertEquals(registrationUserDto.getLogin(), createdUser.getLogin());
        Assertions.assertEquals(encodedPassword, createdUser.getPassword());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(User.class));
    }

}