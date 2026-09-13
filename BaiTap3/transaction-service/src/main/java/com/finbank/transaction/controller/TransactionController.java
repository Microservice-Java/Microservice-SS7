package com.finbank.transaction.controller;

import com.finbank.transaction.dto.AccountResponseDto;
import com.finbank.transaction.dto.TransferRequest;
import com.finbank.transaction.model.Transaction;
import com.finbank.transaction.repository.TransactionRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final RestTemplate restTemplate;
    private final TransactionRepository transactionRepository;

    public TransactionController(RestTemplate restTemplate, TransactionRepository transactionRepository) {
        this.restTemplate = restTemplate;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "transaction-service",
                "status", "UP",
                "port", 8083
        ));
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(transactionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        var txnOpt = transactionRepository.findById(id);
        if (txnOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Giao dịch không tồn tại", "status", 404));
        }
        return ResponseEntity.ok(txnOpt.get());
    }

    // Yeu cau 3: POST /api/transactions/transfer
    @PostMapping("/transfer")
    public ResponseEntity<?> transferMoney(@RequestBody TransferRequest request) {
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Transaction txn = new Transaction();
        txn.setTransactionId(txnId);
        txn.setFromAccountNumber(request.getFromAccountNumber());
        txn.setToAccountNumber(request.getToAccountNumber());
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setCreatedAt(LocalDateTime.now());

        String accountServiceUrl = "http://account-service/api/accounts/";

        // Buoc 1: Kiem tra tai khoan nguon co ton tai va du so du khong
        AccountResponseDto fromAccount;
        try {
            ResponseEntity<AccountResponseDto> fromResp = restTemplate.getForEntity(
                    accountServiceUrl + request.getFromAccountNumber(), AccountResponseDto.class);
            fromAccount = fromResp.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Tài khoản nguồn (" + request.getFromAccountNumber() + ") không tồn tại");
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(txn);
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Lỗi khi kết nối sang Account Service: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(txn);
        }

        if (fromAccount == null || fromAccount.getBalance() == null || fromAccount.getBalance() < request.getAmount()) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Số dư tài khoản nguồn không đủ để thực hiện giao dịch (Hiện có: " +
                    (fromAccount != null ? fromAccount.getBalance() : 0) + " VND, Cần: " + request.getAmount() + " VND)");
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(txn);
        }

        // Buoc 2: Kiem tra tai khoan dich co ton tai khong
        try {
            restTemplate.getForEntity(accountServiceUrl + request.getToAccountNumber(), AccountResponseDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Tài khoản đích (" + request.getToAccountNumber() + ") không tồn tại");
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(txn);
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Lỗi khi kiểm tra tài khoản đích: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(txn);
        }

        // Buoc 3: Goi Account Service tru tien tai khoan nguon
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> debitBody = new HttpEntity<>(Map.of("amount", request.getAmount()), headers);

        try {
            restTemplate.exchange(
                    accountServiceUrl + request.getFromAccountNumber() + "/debit",
                    HttpMethod.PUT,
                    debitBody,
                    Object.class
            );
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Trừ tiền tài khoản nguồn thất bại: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(txn);
        }

        // Buoc 4: Goi Account Service cong tien tai khoan dich
        HttpEntity<Map<String, Object>> creditBody = new HttpEntity<>(Map.of("amount", request.getAmount()), headers);
        try {
            restTemplate.exchange(
                    accountServiceUrl + request.getToAccountNumber() + "/credit",
                    HttpMethod.PUT,
                    creditBody,
                    Object.class
            );
        } catch (Exception e) {
            // Revert (hoan tien lại nguồn nếu cộng đích thất bại)
            restTemplate.exchange(
                    accountServiceUrl + request.getFromAccountNumber() + "/credit",
                    HttpMethod.PUT,
                    debitBody,
                    Object.class
            );
            txn.setStatus("FAILED");
            txn.setErrorMessage("Cộng tiền tài khoản đích thất bại: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(txn);
        }

        // Buoc 5: Luu ban ghi giao dich SUCCESS
        txn.setStatus("SUCCESS");
        txn.setErrorMessage(null);
        Transaction savedTxn = transactionRepository.save(txn);

        return ResponseEntity.ok(savedTxn);
    }
}
