package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.services.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/contacts")
public class PhoneContactsController {
@Autowired
    private ContactService contactService;

    @PostMapping("/new")
    public ResponseEntity<?> addContact(@RequestBody Contact contact, @RequestHeader("Authorization") String authorizationHeader) {
        return contactService.addContact(contact, authorizationHeader);
    }

    @PutMapping("/edit/{name}")
    public ResponseEntity<?> editContact(@PathVariable String name, @RequestBody Contact contact) {
        return contactService.editContact(name, contact);
    }


    @DeleteMapping("/delete/{name}")
    public ResponseEntity<List<Contact>> deleteContactByName(@PathVariable String name) {
        boolean isDeleted=contactService.delete(name);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        List<Contact> remainingContacts = contactService.findAll();
        return ResponseEntity.ok(remainingContacts);
    }



    @GetMapping("/get/{name}")
    public ResponseEntity<?> getContactsByName(@PathVariable String name) {
        Contact contact = contactService.findByName(name);
        return ResponseEntity.ok(Objects.requireNonNullElse(contact, "Contact not found"));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllContacts() {
        List<Contact> contacts = contactService.findAll();
        return ResponseEntity.ok(contacts);
    }
}
