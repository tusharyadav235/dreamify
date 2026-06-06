package com.dreamify.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Adjust based on your Nginx configuration
public class AuthController {

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        Map<String, Object> response = new HashMap<>();

        if ("admin".equals(username) && adminPassword.equals(password)) {
            // Generate token expiring in 24 hours
            String token = JWT.create()
                    .withSubject(username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + 86400000))
                    .sign(Algorithm.HMAC256(jwtSecret));

            response.put("success", true);
            response.put("token", token);
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Incorrect username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
