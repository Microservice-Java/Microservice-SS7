package com.finbank.transaction.client;

import com.finbank.transaction.dto.CustomerResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/api/customers/account/{accountNumber}")
    CustomerResponseDto getCustomerByAccount(@PathVariable("accountNumber") String accountNumber);
}
