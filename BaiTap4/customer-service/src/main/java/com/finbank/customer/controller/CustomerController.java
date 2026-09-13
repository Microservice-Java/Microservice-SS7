package com.finbank.customer.controller;

import com.finbank.customer.model.Customer;
import com.finbank.customer.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "customer-service",
                "status", "UP",
                "port", 8081
        ));
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        var custOpt = customerRepository.findById(id);
        if (custOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Khách hàng không tồn tại", "status", 404));
        }
        return ResponseEntity.ok(custOpt.get());
    }

    // GET /api/customers/account/{accountNumber}
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<?> getCustomerByAccount(@PathVariable String accountNumber) {
        var custOpt = customerRepository.findByAccountNumber(accountNumber);
        if (custOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy khách hàng cho số tài khoản " + accountNumber, "status", 404));
        }
        return ResponseEntity.ok(custOpt.get());
    }
}
