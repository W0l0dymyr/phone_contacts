package com.example.phonecontactsapplication.repositories;

import com.example.phonecontactsapplication.entities.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Contact findByName(String name);


    boolean existsByName(String name);
}
