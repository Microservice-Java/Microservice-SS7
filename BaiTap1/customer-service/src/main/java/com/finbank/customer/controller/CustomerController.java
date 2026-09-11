package com.finbank.customer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCustomers() {
        return ResponseEntity.ok(Map.of(
                "service", "customer-service",
                "port", 8081,
                "data", "List of FinBank customers"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "id", id,
                "fullName", "Nguyen Van Customer",
                "email", "customer@finbank.com",
                "status", "ACTIVE"
        ));
    }
}
