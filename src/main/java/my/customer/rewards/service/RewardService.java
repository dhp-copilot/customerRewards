package my.customer.rewards.service;

import my.customer.rewards.domain.PurchaseTransaction;
import my.customer.rewards.dto.MonthlyRewardReport;
import my.customer.rewards.dto.TotalRewardReport;
import my.customer.rewards.repository.CustomerRepository;
import my.customer.rewards.repository.PurchaseTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class RewardService {
    private final PurchaseTransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final RewardCalculator rewardCalculator;

    public RewardService(PurchaseTransactionRepository transactionRepository,
                         CustomerRepository customerRepository,
                         RewardCalculator rewardCalculator) {
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
        this.rewardCalculator = rewardCalculator;
    }

    public int calculatePoints(BigDecimal amount) {
        return rewardCalculator.pointsFor(amount);
    }

    @Transactional(readOnly = true)
    public MonthlyRewardReport monthly(String username, int year, int month) {
        customerRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + username));
        YearMonth yearMonth = YearMonth.of(year, month);
        List<PurchaseTransaction> transactions = transactionRepository.findForMonth(
                username, yearMonth.atDay(1), yearMonth.plusMonths(1).atDay(1));
        return report(username, yearMonth, transactions);
    }

    @Transactional(readOnly = true)
    public TotalRewardReport total(String username) {
        customerRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + username));
        List<PurchaseTransaction> transactions =
                transactionRepository.findByCustomerUsernameOrderByTransactionDateAscIdAsc(username);
        Map<YearMonth, List<PurchaseTransaction>> grouped = new TreeMap<>();
        for (PurchaseTransaction transaction : transactions) {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            if (!grouped.containsKey(month)) {
                grouped.put(month, new ArrayList<PurchaseTransaction>());
            }
            grouped.get(month).add(transaction);
        }
        List<MonthlyRewardReport> monthly = new ArrayList<>();
        for (Map.Entry<YearMonth, List<PurchaseTransaction>> entry : grouped.entrySet()) {
            monthly.add(report(username, entry.getKey(), entry.getValue()));
        }
        return new TotalRewardReport(username, sumAmount(transactions), sumPoints(transactions), monthly);
    }

    @Transactional(readOnly = true)
    public List<MonthlyRewardReport> monthlyReports(String username) {
        TotalRewardReport total = total(username);
        return total.getMonthly();
    }

    private MonthlyRewardReport report(String username, YearMonth month,
                                       List<PurchaseTransaction> transactions) {
        return new MonthlyRewardReport(username, month.getYear(), month.getMonthValue(),
                sumAmount(transactions), sumPoints(transactions));
    }

    private BigDecimal sumAmount(List<PurchaseTransaction> transactions) {
        BigDecimal amount = BigDecimal.ZERO;
        for (PurchaseTransaction transaction : transactions) {
            amount = amount.add(transaction.getAmount());
        }
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private int sumPoints(List<PurchaseTransaction> transactions) {
        long points = 0;
        for (PurchaseTransaction transaction : transactions) {
            points += rewardCalculator.pointsFor(transaction.getAmount());
        }
        return points > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) points;
    }
}
