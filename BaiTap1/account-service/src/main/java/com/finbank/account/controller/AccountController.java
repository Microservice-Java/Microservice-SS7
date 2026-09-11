package com.finbank.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccounts() {
        return ResponseEntity.ok(Map.of(
                "service", "account-service",
                "port", 8082,
                "data", "List of FinBank accounts"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "id", id,
                "accountNumber", "1010888999",
                "balance", 50000000.0,
                "currency", "VND"
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "message", "Account created successfully via Gateway",
                "accountNumber", "1010999000",
                "status", "ACTIVE"
        ));
    }
}
