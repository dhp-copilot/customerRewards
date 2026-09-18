package my.customer.rewards.service;

import my.customer.rewards.domain.Customer;
import my.customer.rewards.domain.PurchaseTransaction;
import my.customer.rewards.dto.TransactionRequest;
import my.customer.rewards.dto.TransactionResponse;
import my.customer.rewards.exception.ConflictException;
import my.customer.rewards.repository.CustomerRepository;
import my.customer.rewards.repository.PurchaseTransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public TransactionResponse addTransaction(String username, TransactionRequest request,
                                              String createdBy, String clientIp,
                                              String idempotencyKey) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + username));
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key must not be blank");
        }
        PurchaseTransaction existing = transactionRepository
                .findByCustomerUsernameAndIdempotencyKey(username, idempotencyKey)
                .orElse(null);
        if (existing != null) {
            return idempotentResponse(existing, request);
        }

        PurchaseTransaction saved;
        try {
            saved = transactionRepository.saveAndFlush(new PurchaseTransaction(customer,
                    request.getAmount(), request.getTransactionDate(), createdBy, clientIp, idempotencyKey));
        } catch (DataIntegrityViolationException ex) {
            PurchaseTransaction concurrent = transactionRepository
                    .findByCustomerUsernameAndIdempotencyKey(username, idempotencyKey)
                    .orElse(null);
            if (concurrent != null) {
                return idempotentResponse(concurrent, request);
            }
            throw new ConflictException("Transaction could not be created because its idempotency key is already in use");
        }
        return new TransactionResponse(saved, rewardCalculator.pointsFor(saved.getAmount()));
    }

    @Transactional
    public TransactionResponse addTransaction(String username, TransactionRequest request) {
        return addTransaction(username, request, username, null,
                "legacy-" + java.util.UUID.randomUUID().toString());
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

    @Transactional(readOnly = true)
    public Page<TransactionResponse> transactions(String username, Pageable pageable) {
        if (!customerRepository.findByUsername(username).isPresent()) {
            throw new NoSuchElementException("Customer not found: " + username);
        }
        return transactionRepository.findByCustomerUsernameOrderByTransactionDateAscIdAsc(username, pageable)
                .map(t -> new TransactionResponse(t, rewardCalculator.pointsFor(t.getAmount())));
    }

    private TransactionResponse idempotentResponse(PurchaseTransaction existing,
                                                    TransactionRequest request) {
        if (existing.getAmount().compareTo(request.getAmount()) != 0
                || !existing.getTransactionDate().equals(request.getTransactionDate())) {
            throw new ConflictException("Idempotency-Key was already used with a different transaction payload");
        }
        return new TransactionResponse(existing, rewardCalculator.pointsFor(existing.getAmount()));
    }
}
