package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.repositories.ContactRepository;
import com.example.phonecontactsapplication.repositories.UserRepository;
import com.example.phonecontactsapplication.services.ContactService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class PhoneContactsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactService contactService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private String authToken;

    @BeforeEach
    public void setup() throws Exception {
        contactRepository.deleteAll();
        String authRequestJson = "{\"login\":\"login\", \"password\":\"password\"}";

        MvcResult result = mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("token")))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = new ObjectMapper().readTree(responseContent);
        authToken = responseJson.get("token").asText();
    }


    @Test
    public void testAddContact_NotAuthorizedUser() throws Exception {
        // Arrange
        Contact contact = createContact();

        // Act
        mockMvc.perform(post("/contacts/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isUnauthorized());

        assertFalse(contactRepository.existsByName(contact.getName()));
    }

    @Test
    public void testAddContact_InvalidEmailFormat() throws Exception {
        // Arrange
        Contact contact = createContactWithInvalidEmails();

            mockMvc.perform(post("/contacts/new")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(contact)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Invalid email format")));

            assertFalse(contactRepository.existsByName(contact.getName()));

    }

    @Test
    public void testAddContact_InvalidPhoneNumberFormat() throws Exception {
        // Arrange
        Contact contact = createContactWithInvalidPhoneNumbers();

        // Act
        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid phone number format")));

        assertFalse(contactRepository.existsByName(contact.getName()));
    }

    @Test
    public void testAddContact_AuthorizedUser_Success() throws Exception {
        // Arrange
        Contact contact = createContact();

        // Act
        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk());

        assertTrue(contactRepository.existsByName(contact.getName()));
       // contactRepository.deleteAll();
    }

    @Test
    public void testAddContact_NameExist() throws Exception {
        // Arrange
        Contact contact = createContact();

        addContact();
        // Act
        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk()).andExpect(content().string("Contact with that name exists"));

    }

    @Test
    public void testAddContact_EmailExists() throws Exception {
        // Arrange
        Contact contact = createContact();
        contact.setName("Bohdan");
        contact.setPhoneNumbers(Collections.EMPTY_SET);

        addContact();
        // Act
        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk()).andExpect(content().string("Contact with that email exists"));

    }

    @Test
    public void testAddContact_PhoneNumberExists() throws Exception {
        // Arrange
        Contact contact = createContact();
        contact.setName("Bohdan");
        contact.setEmails(Collections.EMPTY_SET);

        addContact();
        // Act
        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk()).andExpect(content().string("Contact with that phone number exists"));

    }
    ////--------------------------------------------------------------
    @Test
    public void testEditContact_NotAuthorizedUser() throws Exception {
        // Arrange
        Contact existingContact = createContact();
         contactRepository.save(existingContact);

        String newName = "New Name";
        Contact updatedContact = new Contact();
        updatedContact.setName(newName);

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact.getName())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isUnauthorized());

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact.getName());
        assertEquals(existingContact.getName(), retrievedContact.getName());
    }

    @Test
    public void testEditContact_ContactNotFound() throws Exception {
        // Arrange
        String contactName = "Non-existent Contact";
        Contact updatedContact = new Contact();
        updatedContact.setName("Updated Name");

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", contactName)
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("does not exist")));

        // Assert
        assertNull(contactRepository.findByName(contactName));
    }

    @Test
    public void testEditContact_NameAlreadyExists() throws Exception {
        // Arrange
        Contact existingContact1 = createContact();
        contactRepository.save(existingContact1);

        Contact existingContact2 = createContact();
        existingContact2.setName("Another Contact");
        contactRepository.save(existingContact2);

        String newName = existingContact2.getName();
        Contact updatedContact = new Contact();
        updatedContact.setName(newName);

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact1.getName())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("already exists")));

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact1.getName());
        assertEquals(existingContact1.getName(), retrievedContact.getName());
    }

    @Test
    public void testEditContact_InvalidEmailFormat() throws Exception {
        // Arrange
        Contact existingContact = createContact();
        contactRepository.save(existingContact);

        Contact updatedContact = new Contact();
        updatedContact.getEmails().add("invalidemail");

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact.getName())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid email format")));

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact.getName());
        assertFalse(existingContact.getEmails().contains("invalidemail"));
    }

    @Test
    public void testEditContact_EmailAlreadyExists() throws Exception {
        // Arrange
        Contact existingContact1 = createContact();
        existingContact1.setEmails(Collections.EMPTY_SET);
        contactRepository.save(existingContact1);

        Contact existingContact2 = createContact();
        existingContact2.setName("Another Contact");
        contactRepository.save(existingContact2);

        String newName = "Updated Name";
        Contact updatedContact = new Contact();
        updatedContact.setName(newName);
        updatedContact.getEmails().addAll(existingContact2.getEmails());

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact1.getName())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("email already exists")));

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact1.getName());
        assertEquals(existingContact1.getName(), retrievedContact.getName());
    }

    @Test
    public void testEditContact_InvalidPhoneNumberFormat() throws Exception {
        // Arrange
        Contact existingContact = createContact();
        contactRepository.save(existingContact);

        Contact updatedContact = new Contact();
        updatedContact.getPhoneNumbers().add("12345678s");

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact.getName())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Invalid phone number format")));

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact.getName());
        assertFalse(existingContact.getPhoneNumbers().contains("12345678s"));
    }

    @Test
    public void testEditContact_PhoneNumberAlreadyExists() throws Exception {
        // Arrange
        Contact existingContact1 = createContact();
        existingContact1.setPhoneNumbers(Collections.EMPTY_SET);
        contactRepository.save(existingContact1);

        Contact existingContact2 = createContact();
        existingContact2.setName("Another Contact");
        contactRepository.save(existingContact2);

        String newName = "Updated Name";
        Contact updatedContact = new Contact();
        updatedContact.setName(newName);
        updatedContact.getPhoneNumbers().addAll(existingContact2.getPhoneNumbers());

        // Act
        mockMvc.perform(put("/contacts/edit/{name}", existingContact1.getName())
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("phone number already exists")));

        // Assert
        Contact retrievedContact = contactRepository.findByName(existingContact1.getName());
        assertEquals(existingContact1.getName(), retrievedContact.getName());
    }
    //-----------------------------------------


    @Test
    public void testDeleteContact_NotAuthorizedUser() throws Exception {
        // Arrange
        addContact();

        // Act
        mockMvc.perform(delete("/contacts/delete/{name}", "Ivan"))
                .andExpect(status().isUnauthorized());

        assertTrue(contactRepository.existsByName("Ivan"));
    }

    @Test
    public void testDeleteContact_AuthorizedUser_Success() throws Exception {
        // Arrange
        addContact();

        // Act
        mockMvc.perform(delete("/contacts/delete/{name}", "Ivan")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk());

        assertFalse(contactRepository.existsByName("Ivan"));
    }

    @Test
    public void testDeleteContact_AuthorizedUser_NotFound() throws Exception {
        // Arrange
        String nonExistingContactName = "NonExistingContact";

        // Act
        mockMvc.perform(delete("/contacts/delete/{name}", nonExistingContactName)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());

        assertFalse(contactRepository.existsByName(nonExistingContactName));
    }
    //---------------------/--------------------------

    @Test
    public void testGetContactByName_ContactExists() throws Exception {
        // Arrange
        Contact contact =addContact();

        // Act
        mockMvc.perform(get("/contacts/get/{name}", "Ivan")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(contact.getName()))
                .andExpect(jsonPath("$.emails", containsInAnyOrder(contact.getEmails().toArray())))
                .andExpect(jsonPath("$.phoneNumbers", containsInAnyOrder(contact.getPhoneNumbers().toArray())));
    }

    @Test
    public void testGetContactByName_ContactNotFound() throws Exception {
        // Arrange
        String contactName = "NonexistentContact";

        // Act
        mockMvc.perform(get("/contacts/get/{name}", contactName)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Contact not found"));
    }

    @Test
    public void testGetContactByName_Unauthorized() throws Exception {
        // Arrange
        Contact contact = addContact();

        // Act
        mockMvc.perform(get("/contacts/get/{name}", contact.getName()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testGetAllContacts() throws Exception {
        // Arrange
        Contact contact1 = createContact();
        Contact contact2 = createContact();
        contact2.setName("John");
        contactRepository.save(contact1);
        contactRepository.save(contact2);

        // Act
        mockMvc.perform(get("/contacts/all")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))

                .andExpect(jsonPath("$[0].name").value(contact1.getName()))
                .andExpect(jsonPath("$[1].name").value(contact2.getName()));
    }

    @Test
    public void testGetAllContacts_Unauthorized() throws Exception {
        // Arrange
        Contact contact1 = createContact();
        Contact contact2 = createContact();
        contact2.setName("John");
        contactRepository.save(contact1);
        contactRepository.save(contact2);

        // Act
        mockMvc.perform(get("/contacts/all"))
                .andExpect(status().isUnauthorized());
    }


    private Contact addContact() throws Exception {
        Contact contact = createContact();

        mockMvc.perform(post("/contacts/new")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(contact)))
                .andExpect(status().isOk());
        return contact;
    }
    private Contact createContact() {
        Contact contact = new Contact();
        Set<String> phoneNumbers = new HashSet<>();
        phoneNumbers.add("123456789");
        phoneNumbers.add("987654321");

        Set<String> emails = new HashSet<>();
        emails.add("example1@example.com");
        emails.add("example2@example.com");

        contact.setId(1L);
        contact.setName("Ivan");
        contact.setPhoneNumbers(phoneNumbers);
        contact.setEmails(emails);
        contact.setUser(userRepository.findByLogin("login"));

        return contact;
    }

    private Contact createContactWithInvalidEmails() {
        Contact contact = createContact();
        contact.getEmails().add("example1example.com");
        contact.getEmails().add("example2@examplecom");
        return contact;
    }

    private Contact createContactWithInvalidPhoneNumbers() {
        Contact contact = createContact();
        contact.getPhoneNumbers().add("12345678s");
        return contact;
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}