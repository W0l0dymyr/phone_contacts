package com.example.phonecontactsapplication.services;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.repositories.ContactRepository;
import com.example.phonecontactsapplication.repositories.UserRepository;
import com.example.phonecontactsapplication.utils.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private JwtTokenUtils tokenUtils;

    @Autowired
    private UserRepository userRepository;

    public List<Contact> findAll() {
        return contactRepository.findAll();
    }

    public Contact findByName(String name) {
        return contactRepository.findByName(name);
    }

    public boolean delete(String name) {
        Contact contact = contactRepository.findByName(name);
        if (contact == null) {
            return false;
        }
        contactRepository.delete(contact);
        return true;
    }

    public boolean isEmailAlreadyExists(String email, Contact existingContact) {
        List<Contact> contacts = contactRepository.findAll();
        for (Contact contact : contacts) {
            if (contact.equals(existingContact)) {
                continue; // Ігноруємо перевірку для існуючого контакту
            }
            Set<String> emails = contact.getEmails();
            if ((emails.contains(email))) {
                return true;
            }
        }
        return false;
    }


    private boolean isEmailAlreadyExists(String email) {
        List<Contact> contacts = contactRepository.findAll();
        for (Contact contact : contacts) {
            Set<String> emails = contact.getEmails();
            if (emails.contains(email)) {
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<?> addContact(Contact contact, String authorizationHeader) {
        if (contactRepository.findByName(contact.getName()) != null) {
            return ResponseEntity.ok("Contact with that name exists");
        }
        for (String email : contact.getEmails()) {
            if (!isValidEmail(email)) {
                return ResponseEntity.ok("Invalid email format: " + email);
            }
        }

        for (String phoneNumber : contact.getPhoneNumbers()) {
            if (!isValidPhoneNumber(phoneNumber)) {
                return ResponseEntity.ok("Invalid phone number format: " + phoneNumber);
            }
        }
        // Перевірка унікальності електронних адрес контакту
        for (String email : contact.getEmails()) {
            if (isEmailAlreadyExists(email)) {
                return ResponseEntity.ok("Contact with that email exists");
            }
        }

        // Перевірка унікальності номерів телефонів контакту
        for (String phoneNumber : contact.getPhoneNumbers()) {
            if (isPhoneNumberAlreadyExists(phoneNumber)) {
                return ResponseEntity.ok("Contact with that phone number exists");
            }
        }
        String token = authorizationHeader.substring(7);
        // Додавання контакту
        contact.setUser(userRepository.findByLogin(tokenUtils.getUsername(token)));
        Contact addedContact = contactRepository.save(contact);
        return ResponseEntity.ok("Contact has been added");
    }

    private boolean isPhoneNumberAlreadyExists(String phoneNumber) {
        List<Contact> contacts = contactRepository.findAll();
        for (Contact contact : contacts) {
            Set<String> phones = contact.getPhoneNumbers();
            if (phones.contains(phoneNumber)) {
                return true;
            }
        }
        return false;
    }


    public boolean isPhoneNumberAlreadyExists(String phoneNumber, Contact existingContact) {
        List<Contact> contacts = contactRepository.findAll();
        for (Contact contact : contacts) {
            if (contact.equals(existingContact)) {
                continue; // Ігноруємо перевірку для існуючого контакту
            }
            Set<String> phoneNumbers = contact.getPhoneNumbers();
            if ((phoneNumbers.contains(phoneNumber))) {
                return true;
            }
        }
        return false;
    }


    public boolean isValidEmail(String email) {
        String emailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
                + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        return email.matches(emailRegex);
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^\\+?[0-9-]+$";
        return phoneNumber.matches(phoneRegex);
    }


    public ResponseEntity<?> editContact(String name, Contact newContact) {
        // Перевірка, чи існує контакт з вказаним ім'ям
        Contact existingContact = contactRepository.findByName(name);
        if (existingContact == null) {
            return ResponseEntity.ok("Contact " + name + " does not exist");
        }

        // Перевірка, чи існує контакт з новим ім'ям, виключаючи поточний контакт
        if (!existingContact.getName().equals(newContact.getName()) && contactRepository.findByName(newContact.getName()) != null) {
            return ResponseEntity.ok("Contact with that name already exists");
        }

        // Перевірка та зміна електронних адрес контакту
        for (String email : newContact.getEmails()) {
            if (!isValidEmail(email)) {
                return ResponseEntity.ok("Invalid email format: " + email);
            }
            if (isEmailAlreadyExists(email, existingContact)) {
                return ResponseEntity.ok("Contact with that email already exists");
            }
        }

        // Перевірка та зміна номерів телефонів контакту
        for (String phoneNumber : newContact.getPhoneNumbers()) {
            if (!isValidPhoneNumber(phoneNumber)) {
                return ResponseEntity.ok("Invalid phone number format: " + phoneNumber);
            }
            if (isPhoneNumberAlreadyExists(phoneNumber, existingContact)) {
                return ResponseEntity.ok("Contact with that phone number already exists");
            }
        }

        // Зміна даних контакту
        existingContact.setName(newContact.getName());
        existingContact.setEmails(newContact.getEmails());
        existingContact.setPhoneNumbers(newContact.getPhoneNumbers());

        // Збереження зміненого контакту
        Contact updatedContact = contactRepository.save(existingContact);
        return ResponseEntity.ok(updatedContact);
    }
}

