package com.example.onlinestore.config;

import com.example.onlinestore.model.Role;
import com.example.onlinestore.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataSeeder {

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void init() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) roleRepository.save(new Role("ROLE_USER"));
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) roleRepository.save(new Role("ROLE_ADMIN"));
        if (roleRepository.findByName("ROLE_MANAGER").isEmpty()) roleRepository.save(new Role("ROLE_MANAGER"));
    }
}
