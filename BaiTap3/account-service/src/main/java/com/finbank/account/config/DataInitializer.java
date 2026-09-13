package com.finbank.account.config;

import com.finbank.account.model.Account;
import com.finbank.account.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) {
        if (accountRepository.findByAccountNumber("1001").isEmpty()) {
            accountRepository.save(new Account(null, "1001", "Nguyen Van A", 10000000.0, "ACTIVE"));
        }
        if (accountRepository.findByAccountNumber("1002").isEmpty()) {
            accountRepository.save(new Account(null, "1002", "Tran Thi B", 5000000.0, "ACTIVE"));
        }
    }
}
