package com.example.multitenantapp.controller;

import com.example.multitenantapp.requestDTO.LoginRequest;
import com.example.multitenantapp.requestDTO.RegisterRequest;
import com.example.multitenantapp.responseDTO.AuthResponse;
import com.example.multitenantapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String response = authService.register(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error"+e.getLocalizedMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            return new ResponseEntity<>("Login failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

