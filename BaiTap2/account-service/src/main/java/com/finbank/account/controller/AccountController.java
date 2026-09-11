package com.finbank.account.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

    @Autowired
    private Environment environment;

    private int getPort() {
        String localPort = environment.getProperty("local.server.port");
        if (localPort != null && !localPort.isEmpty()) {
            return Integer.parseInt(localPort);
        }
        String serverPort = environment.getProperty("server.port");
        if (serverPort != null && !serverPort.isEmpty()) {
            return Integer.parseInt(serverPort);
        }
        return 8082;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAccountInfo() {
        int currentPort = getPort();
        return ResponseEntity.ok(Map.of(
                "service", "account-service",
                "port", currentPort,
                "status", "UP",
                "message", "Account Service instance responding on port " + currentPort
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAccounts() {
        int currentPort = getPort();
        return ResponseEntity.ok(Map.of(
                "service", "account-service",
                "port", currentPort,
                "data", "List of FinBank accounts"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "id", id,
                "accountNumber", "1010888999",
                "balance", 50000000.0,
                "currency", "VND",
                "port", getPort()
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "message", "Account created successfully via Gateway",
                "accountNumber", "1010999000",
                "status", "ACTIVE",
                "port", getPort()
        ));
    }
}
