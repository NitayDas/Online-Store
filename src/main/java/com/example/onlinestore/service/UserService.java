package com.example.onlinestore.service;

import com.example.onlinestore.model.*;
import com.example.onlinestore.repository.*;
import com.example.onlinestore.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String register(String username, String rawPassword, String deptName, Set<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));

        // set department if provided
        if (deptName != null && !deptName.isBlank()) {
            Department dept = departmentRepository.findByName(deptName)
                    .orElseGet(() -> departmentRepository.save(new Department(deptName)));
            user.setDepartment(dept);
        }

        // set roles
        Set<Role> roles = new HashSet<>();
        if (roleNames == null || roleNames.isEmpty()) {
            Role r = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
            roles.add(r);
        } else {
            for (String rn : roleNames) {
                Role r = roleRepository.findByName(rn).orElseGet(() -> roleRepository.save(new Role(rn)));
                roles.add(r);
            }
        }
        user.setRoles(roles);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), roles.stream().map(Role::getName).collect(Collectors.toList()));
        return token;
    }

    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        // if no exception, success:
        User user = userRepository.findByUsername(username).orElseThrow();
        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        return jwtUtil.generateToken(username, roleNames);
    }
}
