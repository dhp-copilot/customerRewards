package my.customer.rewards.config;

import my.customer.rewards.domain.*;
import my.customer.rewards.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner seedData(CustomerRepository customers,
                                      PurchaseTransactionRepository transactions,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (customers.count() != 0) {
                return;
            }
            Customer admin = new Customer("admin", passwordEncoder.encode("password"), my.customer.rewards.domain.Role.ADMIN);
            Customer alice = new Customer("alice", passwordEncoder.encode("password"), my.customer.rewards.domain.Role.CUSTOMER);
            Customer bob = new Customer("bob", passwordEncoder.encode("password"), my.customer.rewards.domain.Role.CUSTOMER);
            customers.saveAll(Arrays.asList(admin, alice, bob));
            transactions.saveAll(Arrays.asList(
                    new PurchaseTransaction(alice, new BigDecimal("120.00"), LocalDate.of(2024, 1, 15)),
                    new PurchaseTransaction(alice, new BigDecimal("75.00"), LocalDate.of(2024, 1, 28)),
                    new PurchaseTransaction(alice, new BigDecimal("200.00"), LocalDate.of(2024, 2, 10)),
                    new PurchaseTransaction(bob, new BigDecimal("50.00"), LocalDate.of(2024, 1, 5)),
                    new PurchaseTransaction(bob, new BigDecimal("100.00"), LocalDate.of(2024, 2, 5)),
                    new PurchaseTransaction(bob, new BigDecimal("120.99"), LocalDate.of(2024, 2, 20)),
                    new PurchaseTransaction(bob, new BigDecimal("300.00"), LocalDate.of(2024, 3, 10))
            ));
        };
    }
}
