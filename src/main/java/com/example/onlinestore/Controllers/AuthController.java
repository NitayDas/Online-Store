package com.example.onlinestore.controller;

import com.example.onlinestore.service.UserService;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Set;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    static class RegisterRequest {
        @NotBlank public String username;
        @NotBlank public String password;
        public String department;
        public Set<String> roles;
    }

    static class LoginRequest {
        @NotBlank public String username;
        @NotBlank public String password;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        String token = userService.register(req.username, req.password, req.department, req.roles);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        String token = userService.login(req.username, req.password);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
