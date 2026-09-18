package my.customer.rewards.service;

import my.customer.rewards.domain.Customer;
import my.customer.rewards.domain.PurchaseTransaction;
import my.customer.rewards.dto.TransactionRequest;
import my.customer.rewards.dto.TransactionResponse;
import my.customer.rewards.repository.CustomerRepository;
import my.customer.rewards.repository.PurchaseTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PurchaseTransactionRepository transactionRepository;
    private final RewardCalculator rewardCalculator;

    public CustomerService(CustomerRepository customerRepository,
                           PurchaseTransactionRepository transactionRepository,
                           RewardCalculator rewardCalculator) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.rewardCalculator = rewardCalculator;
    }

    @Transactional
    public TransactionResponse addTransaction(String username, TransactionRequest request) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + username));
        PurchaseTransaction saved = transactionRepository.save(
                new PurchaseTransaction(customer, request.getAmount(), request.getTransactionDate()));
        return new TransactionResponse(saved, rewardCalculator.pointsFor(saved.getAmount()));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> transactions(String username) {
        if (!customerRepository.findByUsername(username).isPresent()) {
            throw new NoSuchElementException("Customer not found: " + username);
        }
        return transactionRepository.findByCustomerUsernameOrderByTransactionDateAscIdAsc(username)
                .stream().map(t -> new TransactionResponse(t, rewardCalculator.pointsFor(t.getAmount())))
                .collect(Collectors.toList());
    }
}
