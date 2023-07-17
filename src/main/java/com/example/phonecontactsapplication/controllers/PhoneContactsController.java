package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.services.ContactService;
import com.example.phonecontactsapplication.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/contacts")
public class PhoneContactsController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/new")
    public ResponseEntity<?> addContact(@RequestBody Contact contact, @RequestHeader("Authorization") String authorizationHeader) throws IOException {
        return contactService.addContact(contact, authorizationHeader, getAuthenticatedUser());
    }

    @PutMapping("/edit/{name}")
    public ResponseEntity<?> editContact(@PathVariable String name, @RequestBody Contact contact) {
        return contactService.editContact(name, contact, getAuthenticatedUser());
    }


    @DeleteMapping("/delete/{name}")
    public ResponseEntity<List<Contact>> deleteContactByName(@PathVariable String name) {
        boolean isDeleted = contactService.delete(name, getAuthenticatedUser());
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        List<Contact> remainingContacts = contactService.findAll(getAuthenticatedUser());
        return ResponseEntity.ok(remainingContacts);
    }


    @GetMapping("/get/{name}")
    public ResponseEntity<?> getContactsByName(@PathVariable String name) {
        Contact contact = contactService.findByName(name, getAuthenticatedUser());
        return ResponseEntity.ok(Objects.requireNonNullElse(contact, "Contact not found"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllContacts() {

        List<Contact> contacts = contactService.findAll(getAuthenticatedUser());
        return ResponseEntity.ok(contacts);
    }

    private User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String login = authentication.getName();
        Optional<User> user = userService.findByLogin(login);
        return user.get();
    }
}
