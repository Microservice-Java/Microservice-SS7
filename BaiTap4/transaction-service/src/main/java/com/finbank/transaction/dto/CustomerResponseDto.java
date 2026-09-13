package com.finbank.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {
    private Long id;
    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private String accountNumber;
}
