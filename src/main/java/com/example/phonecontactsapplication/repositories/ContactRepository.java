package com.example.phonecontactsapplication.repositories;

import com.example.phonecontactsapplication.entities.Contact;
import com.example.phonecontactsapplication.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Contact findByName(String name);


    boolean existsByName(String name);

    List<Contact> findByUser(User user);
}
