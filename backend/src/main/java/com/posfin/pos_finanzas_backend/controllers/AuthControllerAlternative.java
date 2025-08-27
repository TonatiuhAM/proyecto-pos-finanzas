package com.posfin.pos_finanzas_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthControllerAlternative {

    @Autowired
    private AuthController authController;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // Delegar al controlador principal
        return authController.login(credentials);
    }
}