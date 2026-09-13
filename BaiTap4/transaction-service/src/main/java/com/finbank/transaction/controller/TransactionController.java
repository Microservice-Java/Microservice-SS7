package com.finbank.transaction.controller;

import com.finbank.transaction.client.AccountServiceClient;
import com.finbank.transaction.client.CustomerServiceClient;
import com.finbank.transaction.dto.*;
import com.finbank.transaction.model.Transaction;
import com.finbank.transaction.repository.TransactionRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final AccountServiceClient accountServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final TransactionRepository transactionRepository;

    public TransactionController(AccountServiceClient accountServiceClient,
                                 CustomerServiceClient customerServiceClient,
                                 TransactionRepository transactionRepository) {
        this.accountServiceClient = accountServiceClient;
        this.customerServiceClient = customerServiceClient;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        return ResponseEntity.ok(Map.of(
                "service", "transaction-service",
                "status", "UP",
                "port", 8083,
                "client", "OpenFeign"
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

    // Yeu cau 5.2: GET /api/transactions/{id}/detail - Tra ve thong tin giao dich kem ten khach hang
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getTransactionDetail(@PathVariable Long id) {
        var txnOpt = transactionRepository.findById(id);
        if (txnOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Giao dịch không tồn tại", "status", 404));
        }

        Transaction txn = txnOpt.get();
        TransactionDetailResponse detail = new TransactionDetailResponse();
        detail.setId(txn.getId());
        detail.setTransactionId(txn.getTransactionId());
        detail.setFromAccountNumber(txn.getFromAccountNumber());
        detail.setToAccountNumber(txn.getToAccountNumber());
        detail.setAmount(txn.getAmount());
        detail.setDescription(txn.getDescription());
        detail.setStatus(txn.getStatus());
        detail.setErrorMessage(txn.getErrorMessage());
        detail.setCreatedAt(txn.getCreatedAt());

        // Lấy tên khách hàng nguồn
        try {
            CustomerResponseDto fromCust = customerServiceClient.getCustomerByAccount(txn.getFromAccountNumber());
            if (fromCust != null) {
                detail.setFromCustomerName(fromCust.getFullName());
            }
        } catch (Exception e) {
            detail.setFromCustomerName("N/A (" + txn.getFromAccountNumber() + ")");
        }

        // Lấy tên khách hàng đích
        try {
            CustomerResponseDto toCust = customerServiceClient.getCustomerByAccount(txn.getToAccountNumber());
            if (toCust != null) {
                detail.setToCustomerName(toCust.getFullName());
            }
        } catch (Exception e) {
            detail.setToCustomerName("N/A (" + txn.getToAccountNumber() + ")");
        }

        return ResponseEntity.ok(detail);
    }

    // Yeu cau 4: Refactor POST /api/transactions/transfer bang FeignClient
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

        // Buoc 1: Kiem tra tai khoan nguon bang FeignClient
        AccountResponseDto fromAccount;
        try {
            fromAccount = accountServiceClient.getAccountByNumber(request.getFromAccountNumber());
        } catch (FeignException.NotFound e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Tài khoản nguồn (" + request.getFromAccountNumber() + ") không tồn tại");
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(txn);
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Lỗi khi gọi Account Service qua FeignClient: " + e.getMessage());
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

        // Buoc 2: Kiem tra tai khoan dich bang FeignClient
        try {
            accountServiceClient.getAccountByNumber(request.getToAccountNumber());
        } catch (FeignException.NotFound e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Tài khoản đích (" + request.getToAccountNumber() + ") không tồn tại");
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(txn);
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Lỗi khi kiểm tra tài khoản đích qua FeignClient: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(txn);
        }

        // Buoc 3: Tru tien tai khoan nguon qua FeignClient
        try {
            accountServiceClient.debitAccount(request.getFromAccountNumber(), new AmountRequest(request.getAmount()));
        } catch (Exception e) {
            txn.setStatus("FAILED");
            txn.setErrorMessage("Trừ tiền tài khoản nguồn thất bại: " + e.getMessage());
            transactionRepository.save(txn);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(txn);
        }

        // Buoc 4: Cong tien tai khoan dich qua FeignClient
        try {
            accountServiceClient.creditAccount(request.getToAccountNumber(), new AmountRequest(request.getAmount()));
        } catch (Exception e) {
            // Revert (hoan tien nguon)
            accountServiceClient.creditAccount(request.getFromAccountNumber(), new AmountRequest(request.getAmount()));
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
