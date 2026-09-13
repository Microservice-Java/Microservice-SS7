package com.finbank.transaction.client;

import com.finbank.transaction.dto.AccountResponseDto;
import com.finbank.transaction.dto.AmountRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "account-service")
public interface AccountServiceClient {

    @GetMapping("/api/accounts/{accountNumber}")
    AccountResponseDto getAccountByNumber(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/accounts/{accountNumber}/balance")
    Map<String, Object> getBalance(@PathVariable("accountNumber") String accountNumber);

    @PutMapping("/api/accounts/{accountNumber}/debit")
    AccountResponseDto debitAccount(@PathVariable("accountNumber") String accountNumber, @RequestBody AmountRequest amountRequest);

    @PutMapping("/api/accounts/{accountNumber}/credit")
    AccountResponseDto creditAccount(@PathVariable("accountNumber") String accountNumber, @RequestBody AmountRequest amountRequest);
}
