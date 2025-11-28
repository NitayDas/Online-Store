package com.example.onlinestore.controller;

import com.example.onlinestore.model.Role;
import com.example.onlinestore.repository.RoleRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {
    private final RoleRepository roleRepository;
    public RoleController(RoleRepository roleRepository) { this.roleRepository = roleRepository; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list() { return ResponseEntity.ok(roleRepository.findAll()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Role r) {
        return ResponseEntity.ok(roleRepository.save(r));
    }
}
