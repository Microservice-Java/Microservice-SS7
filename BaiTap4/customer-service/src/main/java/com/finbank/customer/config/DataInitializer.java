package com.finbank.customer.config;

import com.finbank.customer.model.Customer;
import com.finbank.customer.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    public DataInitializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) {
        if (customerRepository.findByAccountNumber("1001").isEmpty()) {
            customerRepository.save(new Customer(null, "CUST001", "Nguyen Van A", "nguyenvana@finbank.com", "0901234567", "1001"));
        }
        if (customerRepository.findByAccountNumber("1002").isEmpty()) {
            customerRepository.save(new Customer(null, "CUST002", "Tran Thi B", "tranthib@finbank.com", "0908765432", "1002"));
        }
    }
}
