package com.finbank.account.controller;

import com.finbank.account.dto.AmountRequest;
import com.finbank.account.model.Account;
import com.finbank.account.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "account-service",
                "status", "UP",
                "port", 8082
        ));
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        if (account.getStatus() == null) {
            account.setStatus("ACTIVE");
        }
        if (account.getBalance() == null) {
            account.setBalance(0.0);
        }
        Account saved = accountRepository.save(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Yeu cau 1.1: GET /api/accounts/{accountNumber} - lay thong tin tai khoan
    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccountByNumber(@PathVariable String accountNumber) {
        var accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Tài khoản " + accountNumber + " không tồn tại", "status", 404));
        }
        return ResponseEntity.ok(accountOpt.get());
    }

    // Yeu cau 1.2: GET /api/accounts/{accountNumber}/balance - lay so du tai khoan
    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<?> getBalance(@PathVariable String accountNumber) {
        var accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Tài khoản " + accountNumber + " không tồn tại", "status", 404));
        }
        Account acc = accountOpt.get();
        return ResponseEntity.ok(Map.of("accountNumber", acc.getAccountNumber(), "balance", acc.getBalance()));
    }

    // Yeu cau 1.3: PUT /api/accounts/{accountNumber}/debit - tru tien
    @PutMapping("/{accountNumber}/debit")
    public ResponseEntity<?> debitAccount(@PathVariable String accountNumber, @RequestBody AmountRequest request) {
        var accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Tài khoản " + accountNumber + " không tồn tại", "status", 404));
        }

        Account account = accountOpt.get();
        double amount = request != null && request.getAmount() != null ? request.getAmount() : 0.0;
        if (account.getBalance() < amount) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Số dư tài khoản không đủ để thực hiện giao dịch", "status", 400));
        }

        account.setBalance(account.getBalance() - amount);
        Account updated = accountRepository.save(account);
        return ResponseEntity.ok(updated);
    }

    // Yeu cau 1.4: PUT /api/accounts/{accountNumber}/credit - cong tien
    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<?> creditAccount(@PathVariable String accountNumber, @RequestBody AmountRequest request) {
        var accountOpt = accountRepository.findByAccountNumber(accountNumber);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Tài khoản " + accountNumber + " không tồn tại", "status", 404));
        }

        Account account = accountOpt.get();
        double amount = request != null && request.getAmount() != null ? request.getAmount() : 0.0;
        account.setBalance(account.getBalance() + amount);
        Account updated = accountRepository.save(account);
        return ResponseEntity.ok(updated);
    }
}
