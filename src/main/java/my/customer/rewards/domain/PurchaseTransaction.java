package my.customer.rewards.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_transactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_purchase_customer_idempotency",
                columnNames = {"customer_id", "idempotency_key"}),
        indexes = {
                @Index(name = "idx_purchase_customer_idempotency",
                        columnList = "customer_id, idempotency_key"),
                @Index(name = "idx_purchase_customer_date",
                        columnList = "customer_id, transaction_date, id")
        })
public class PurchaseTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    protected PurchaseTransaction() {
    }

    public PurchaseTransaction(Customer customer, BigDecimal amount, LocalDate transactionDate) {
        this(customer, amount, transactionDate, "system", null, null);
    }

    public PurchaseTransaction(Customer customer, BigDecimal amount, LocalDate transactionDate,
                               String createdBy, String clientIp, String idempotencyKey) {
        this.customer = customer;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
        this.clientIp = clientIp;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getClientIp() { return clientIp; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
