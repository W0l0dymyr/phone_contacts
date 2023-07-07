package com.example.phonecontactsapplication.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String name;
    @ElementCollection
    @CollectionTable(name = "emails", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "email")
    private Set<String> emails = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "phone_numbers", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "phone_number")
    private Set<String> phoneNumbers = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
//    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Email> emails = new ArrayList<>();
//
//    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<PhoneNumber> phoneNumbers = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails;
    }

    public Set<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(Set<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

//    public List<Email> getEmails() {
//        return emails;
//    }
//
//    public void setEmails(List<Email> emails) {
//        this.emails = emails;
//    }
//
//    public List<PhoneNumber> getPhoneNumbers() {
//        return phoneNumbers;
//    }
//
//    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
//        this.phoneNumbers = phoneNumbers;
//    }
}
