package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.entities.User;
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

import java.io.IOException;
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
    User user = new User();
    List<Contact> contacts = new ArrayList<>();
    contacts.add(new Contact());
    contacts.add(new Contact());
    user.getContacts().add(new Contact());
    user.getContacts().add(new Contact());

    Mockito.when(contactRepository.findByUser(user)).thenReturn(contacts);

    // Act
    List<Contact> result = contactService.findAll(user);

    // Assert
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals(contacts, result);
}

    @Test
    public void testFindByName_ExistingContact() {
        // Arrange
        String name = "John Doe";
        User user = new User();
        Contact contact = new Contact();
        contact.setName(name);
        user.getContacts().add(contact);

        // Act
        Contact result = contactService.findByName(name, user);

        // Assert
        Assertions.assertEquals(contact, result);
    }

    @Test
    public void testFindByName_NonExistingContact() {
        // Arrange
        String name = "John Doe";
        User user = new User();

        Mockito.when(contactRepository.findByName(name)).thenReturn(null);

        // Act
        Contact result = contactService.findByName(name, user);

        // Assert
        Assertions.assertNull(result);
    }

    @Test
    public void testDelete_ExistingContact() {
        // Arrange
        String name = "John Doe";
        User user = new User();
        Contact contact = new Contact();
        contact.setName(name);
        user.getContacts().add(contact);

        // Act
        boolean result = contactService.delete(name, user);

        // Assert
        Assertions.assertTrue(result);
        Mockito.verify(contactRepository, Mockito.times(1)).delete(contact);
    }

    @Test
    public void testDelete_NonExistingContact() {
        // Arrange
        String name = "John Doe";
        User user = new User();

        Mockito.when(contactRepository.findByName(name)).thenReturn(null);

        // Act
        boolean result = contactService.delete(name, user);

        // Assert
        Assertions.assertFalse(result);
        Mockito.verify(contactRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test
    public void testIsEmailAlreadyExists_EmailNotExists() {
        // Arrange
        String email = "john@example.com";
        Contact existingContact = new Contact();
        User user = new User();
        List<Contact> contacts = new ArrayList<>();
        contacts.add(existingContact);
        user.setContacts(contacts);

        // Act
        boolean result = contactService.isEmailAlreadyExists(email, existingContact, user);

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

        User user = new User();
        user.setContacts(contacts);

        // Act
        boolean result = contactService.isEmailAlreadyExists(newEmail, existingContact, user);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testAddContact_ValidContact() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        user.setContacts(Collections.emptyList());

        Mockito.when(contactRepository.save(contact)).thenReturn(contact);

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact has been added", response.getBody());
    }

    @Test
    public void testAddContact_ExistingName() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        Contact existingContact = new Contact();
        existingContact.setName("John Doe");
        user.setContacts(Collections.singletonList(existingContact));

        Mockito.when(contactRepository.save(contact)).thenReturn(contact);

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact with that name exists", response.getBody());
    }

    @Test
    public void testAddContact_InvalidEmailFormat() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("invalid-email"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        user.setContacts(Collections.emptyList());

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Invalid email format: invalid-email", response.getBody());
    }

    @Test
    public void testAddContact_ExistingEmail() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        Contact existingContact = new Contact();
        existingContact.setName("Stephen");
        existingContact.setEmails(Collections.singleton("john.doe@example.com"));
        user.getContacts().add(existingContact);


        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact with that email exists", response.getBody());
    }

    @Test
    public void testAddContact_InvalidPhoneNumberFormat() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123-456-78s"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        user.setContacts(Collections.emptyList());

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Invalid phone number format: 123-456-78s", response.getBody());
    }

    @Test
    public void testAddContact_ExistingPhoneNumber() throws IOException {
        // Arrange
        Contact contact = new Contact();
        contact.setName("John Doe");
        contact.setEmails(Collections.singleton("john.doe@example.com"));
        contact.setPhoneNumbers(Collections.singleton("123456789"));

        String authorizationHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6W10sInN1YiI6ImxvZ2luIiwiaWF0IjoxNjg4OTI2ODQzLCJleHAiOjE2ODg5MzI4NDN9.ASyYMRZyPWpjh0npL_zvs01zMHDCfNtYEkjAmsY-Syg";

        User user = new User();
        Contact existingContact = new Contact();
        existingContact.setName("Stephen");
        existingContact.setPhoneNumbers(Collections.singleton("123456789"));
        user.setContacts(Collections.singletonList(existingContact));

        // Act
        ResponseEntity<?> response = contactService.addContact(contact, authorizationHeader, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact with that phone number exists", response.getBody());
    }

    @Test
    public void testIsValidEmail_ValidEmail() {
        // Arrange
        String email = "john.doe@example.com";

        // Act
        boolean result = contactService.isValidEmail(email);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsValidEmail_InvalidEmail() {
        // Arrange
        String email = "invalid-email";

        // Act
        boolean result = contactService.isValidEmail(email);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    public void testIsValidPhoneNumber_ValidPhoneNumber() {
        // Arrange
        String phoneNumber = "123456789";

        // Act
        boolean result = contactService.isValidPhoneNumber(phoneNumber);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsValidPhoneNumber_InvalidPhoneNumber() {
        // Arrange
        String phoneNumber = "123-456-78s";

        // Act
        boolean result = contactService.isValidPhoneNumber(phoneNumber);

        // Assert
        Assertions.assertFalse(result);
    }

    @Test
    public void testEditContact_ExistingContact_ValidChanges() {
        // Arrange
        String name = "John Doe";
        Contact existingContact = new Contact();
        existingContact.setName(name);
        existingContact.setEmails(Collections.singleton("john.doe@example.com"));
        existingContact.setPhoneNumbers(Collections.singleton("123456789"));

        Contact newContact = new Contact();
        newContact.setName("Jane Smith");
        newContact.setEmails(Collections.singleton("jane.smith@example.com"));
        newContact.setPhoneNumbers(Collections.singleton("987654321"));

        User user = new User();
        user.setContacts(Collections.singletonList(existingContact));

        Mockito.when(contactRepository.findByName(name)).thenReturn(existingContact);
        Mockito.when(contactRepository.findByName(newContact.getName())).thenReturn(null);
        Mockito.when(contactRepository.save(existingContact)).thenReturn(existingContact);

        // Act
        ResponseEntity<?> response = contactService.editContact(name, newContact, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(newContact, response.getBody());
        Assertions.assertEquals(newContact.getName(), existingContact.getName());
        Assertions.assertEquals(newContact.getEmails(), existingContact.getEmails());
        Assertions.assertEquals(newContact.getPhoneNumbers(), existingContact.getPhoneNumbers());
    }


    @Test
    public void testEditContact_NonExistingContact() {
        // Arrange
        String name = "John Doe";
        Contact newContact = new Contact();
        newContact.setName("Jane Smith");

        User user = new User();

        Mockito.when(contactRepository.findByName(name)).thenReturn(null);

        // Act
        ResponseEntity<?> response = contactService.editContact(name, newContact, user);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Contact " + name + " does not exist", response.getBody());
    }


}