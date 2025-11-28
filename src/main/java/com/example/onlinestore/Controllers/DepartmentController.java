package com.example.onlinestore.controller;

import com.example.onlinestore.model.Department;
import com.example.onlinestore.repository.DepartmentRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/admin/departments")
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list() { return ResponseEntity.ok(departmentRepository.findAll()); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Department body) {
        Department d = departmentRepository.save(body);
        return ResponseEntity.ok(d);
    }
}
