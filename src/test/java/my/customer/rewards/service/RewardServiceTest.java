package my.customer.rewards.service;

import my.customer.rewards.domain.*;
import my.customer.rewards.dto.TotalRewardReport;
import my.customer.rewards.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class RewardServiceTest {
    @Mock private PurchaseTransactionRepository transactions;
    @Mock private CustomerRepository customers;

    @Test
    void aggregatesPointsAndAmountsByMonthAndTotal() {
        Customer alice = new Customer("alice", "encoded", Role.CUSTOMER);
        List<PurchaseTransaction> purchases = Arrays.asList(
                new PurchaseTransaction(alice, new BigDecimal("120.00"), LocalDate.of(2024, 1, 2)),
                new PurchaseTransaction(alice, new BigDecimal("75.00"), LocalDate.of(2024, 1, 3)),
                new PurchaseTransaction(alice, new BigDecimal("200.00"), LocalDate.of(2024, 2, 2)));
        when(customers.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(transactions.findByCustomerUsernameOrderByTransactionDateAscIdAsc("alice")).thenReturn(purchases);

        TotalRewardReport report = new RewardService(transactions, customers,
                new RewardCalculator(new my.customer.rewards.config.RewardTierProperties())).total("alice");

        assertEquals(new BigDecimal("395.00"), report.getTotalAmount());
        assertEquals(90 + 25 + 250, report.getRewardPoints());
        assertEquals(2, report.getMonthly().size());
        assertEquals(115, report.getMonthly().get(0).getRewardPoints());
        assertEquals(new BigDecimal("200.00"), report.getMonthly().get(1).getTotalAmount());
    }
}
