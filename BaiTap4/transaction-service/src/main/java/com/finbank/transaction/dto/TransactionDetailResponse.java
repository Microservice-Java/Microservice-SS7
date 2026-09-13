package com.finbank.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {
    private Long id;
    private String transactionId;
    private String fromAccountNumber;
    private String fromCustomerName;
    private String toAccountNumber;
    private String toCustomerName;
    private Double amount;
    private String description;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
