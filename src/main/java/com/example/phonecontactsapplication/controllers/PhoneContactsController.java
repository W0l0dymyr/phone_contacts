package com.example.phonecontactsapplication.controllers;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.entities.User;
import com.example.phonecontactsapplication.repositories.ContactRepository;
import com.example.phonecontactsapplication.services.ContactService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
public class PhoneContactsController {
@Autowired
    private ContactService contactService;

    @PostMapping("/new")
    public ResponseEntity<?> addContact(@RequestBody Contact contact, @AuthenticationPrincipal User user) {
        return contactService.addContact(contact, user);
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
    public ResponseEntity<Contact> getContactsByName(@PathVariable String name) {
        Contact contact = contactService.findByName(name);
        if (contact==null) {
            ResponseEntity.ok("Contact not found");
        }
        return ResponseEntity.ok(contact);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = contactService.findAll();
        return ResponseEntity.ok(contacts);
    }
}
