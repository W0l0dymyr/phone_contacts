package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.repositories.ContactRepository;
import com.example.phonecontactsapplication.repositories.UserRepository;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class ContactServiceTest {
    @Mock
    private ContactRepository contactRepository;

    @Mock
    private JwtTokenUtils tokenUtils;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    @Test
    public void testFindAll() {
        // Arrange
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact());
        contacts.add(new Contact());

        Mockito.when(contactRepository.findAll()).thenReturn(contacts);

        // Act
        List<Contact> result = contactService.findAll();

        // Assert
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(contacts, result);
    }

    @Test
    public void testFindByName_ExistingContact() {
        // Arrange
        String name = "John Doe";
        Contact contact = new Contact();
        contact.setName(name);

        Mockito.when(contactRepository.findByName(name)).thenReturn(contact);

        // Act
        Contact result = contactService.findByName(name);

        // Assert
        Assertions.assertEquals(contact, result);
    }

    @Test
    public void testFindByName_NonExistingContact() {
        // Arrange
        String name = "John Doe";

        Mockito.when(contactRepository.findByName(name)).thenReturn(null);

        // Act
        Contact result = contactService.findByName(name);

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    public void testDelete_ExistingContact() {
        // Arrange
        String name = "John Doe";
        Contact contact = new Contact();
        contact.setName(name);

        Mockito.when(contactRepository.findByName(name)).thenReturn(contact);

        // Act
        boolean result = contactService.delete(name);

        // Assert
        Assertions.assertTrue(result);
        Mockito.verify(contactRepository, Mockito.times(1)).delete(contact);
    }

    @Test
    public void testDelete_NonExistingContact() {
        // Arrange
        String name = "John Doe";

        Mockito.when(contactRepository.findByName(name)).thenReturn(null);

        // Act
        boolean result = contactService.delete(name);

        // Assert
        Assertions.assertFalse(result);
        Mockito.verify(contactRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void testIsEmailAlreadyExists_EmailNotExists() {
        // Arrange
        String email = "john@example.com";
        Contact existingContact = new Contact();

        Mockito.when(contactRepository.findAll()).thenReturn(Collections.singletonList(existingContact));

        // Act
        boolean result = contactService.isEmailAlreadyExists(email, existingContact);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    public void testIsEmailAlreadyExists_EmailExists() {
        // Arrange
        String newEmail = "john@example.com";
        Contact existingContact = new Contact();
        existingContact.setEmails(Collections.singleton("existingemail@example.com"));

        Contact otherContact = new Contact();
        otherContact.setEmails(Collections.singleton("john@example.com"));

        List<Contact> contacts = new ArrayList<>();
        contacts.add(existingContact);
        contacts.add(otherContact);

        Mockito.when(contactRepository.findAll()).thenReturn(contacts);

        // Act
        boolean result = contactService.isEmailAlreadyExists(newEmail, existingContact);

        // Assert
        Assertions.assertTrue(result);
    }



    @Test
    public void testAddContact_ValidContact() {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        Mockito.when(contactRepository.findByName(contact.getName())).thenReturn(null);
        Mockito.when(contactRepository.save(contact)).thenReturn(contact);

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact has been added", response.getBody());
    }

    @Test
    public void testAddContact_ExistingName() {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = null;

        Mockito.when(contactRepository.findByName(contact.getName())).thenReturn(contact);

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact with that name exists", response.getBody());
    }

    @Test
    public void testAddContact_InvalidEmail() {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("invalid-email"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Header";

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Invalid email format: invalid-email", response.getBody());
    }

    @Test
    public void testAddContact_ExistingEmail() {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Header";

        Mockito.when(contactRepository.findByName(contact.getName())).thenReturn(null);
        Mockito.when(contactRepository.save(contact)).thenReturn(contact);
        Mockito.when(contactRepository.findAll()).thenReturn(Collections.singletonList(contact));

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact with that email exists", response.getBody());
    }

    @Test
    public void testIsPhoneNumberAlreadyExists_PhoneNumberExists() {
        // Arrange
        String newPhoneNumber = "123456789";
        Contact existingContact = new Contact();
        existingContact.setPhoneNumbers(Collections.singleton("987654321"));
        Contact otherContact = new Contact();
        otherContact.setPhoneNumbers(Collections.singleton(newPhoneNumber));

        List<Contact> contacts = new ArrayList<>();
        contacts.add(existingContact);
        contacts.add(otherContact);

        Mockito.when(contactRepository.findAll()).thenReturn(contacts);

        // Act
        boolean result = contactService.isPhoneNumberAlreadyExists(newPhoneNumber, existingContact);

        // Assert
        Assertions.assertTrue(result);
    }


    @Test
    public void testIsPhoneNumberAlreadyExists_PhoneNumberDoesNotExist() {
        // Arrange
        String phoneNumber = "987654321";
        Contact existingContact = new Contact();
        existingContact.setPhoneNumbers(Collections.singleton("123456789"));

        Mockito.when(contactRepository.findAll()).thenReturn(Collections.singletonList(existingContact));

        // Act
        boolean result = contactService.isPhoneNumberAlreadyExists(phoneNumber, existingContact);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    public void testIsValidEmail_ValidEmail() {
        // Arrange
        String email = "john@example.com";

        // Act
        boolean result = contactService.isValidEmail(email);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsValidEmail_InvalidEmail() {
        // Arrange
        String[] invalidEmails = {
                ".user.name@domain.com",
                "user-name@domain.com.",
                "username@.com"
        };

        // Act & Assert
        for (String email : invalidEmails) {
            boolean result = contactService.isValidEmail(email);
            Assertions.assertFalse(result);
        }
    }


    @Test
    public void testIsValidPhoneNumber_ValidPhoneNumber() {
        // Arrange
        String phoneNumber = "+123456789";

        // Act
        boolean result = contactService.isValidPhoneNumber(phoneNumber);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsValidPhoneNumber_InvalidPhoneNumber() {
        // Arrange
        String[] invalidPhoneNumbers = {
                "+38-asdas",
                "+123456789a"
        };

        // Act & Assert
        for (String phoneNumber : invalidPhoneNumbers) {
            boolean result = contactService.isValidPhoneNumber(phoneNumber);
            Assertions.assertFalse(result);
        }
    }

    @Test
    public void testEditContact_ValidData() {
        // Arrange
        String existingName = "John";
        Contact existingContact = new Contact();
        existingContact.setName(existingName);
        existingContact.setEmails(Collections.singleton("john@example.com"));
        existingContact.setPhoneNumbers(Collections.singleton("+123456789"));

        String newName = "John Doe";
        Contact newContact = new Contact();
        newContact.setName(newName);
        newContact.setEmails(Collections.singleton("johndoe@example.com"));
        newContact.setPhoneNumbers(Collections.singleton("+987654321"));

        Mockito.when(contactRepository.findByName(existingName)).thenReturn(existingContact);
        Mockito.when(contactRepository.findByName(newName)).thenReturn(null);
        Mockito.when(contactRepository.save(existingContact)).thenReturn(existingContact);

        // Act
        ResponseEntity<?> response = contactService.editContact(existingName, newContact);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(existingContact, response.getBody());
        Assertions.assertEquals(newName, existingContact.getName());
        Assertions.assertEquals(Collections.singleton("johndoe@example.com"), existingContact.getEmails());
        Assertions.assertEquals(Collections.singleton("+987654321"), existingContact.getPhoneNumbers());
    }

}