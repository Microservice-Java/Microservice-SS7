package com.finbank.transaction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTransactions() {
        return ResponseEntity.ok(Map.of(
                "service", "transaction-service",
                "port", 8083,
                "data", "List of FinBank transactions"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "id", id,
                "transactionId", "TXN998877",
                "amount", 1000000.0,
                "type", "TRANSFER"
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createTransaction(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of(
                "message", "Money transferred successfully via Gateway",
                "transactionId", "TXN998878",
                "status", "SUCCESS"
        ));
    }
}
