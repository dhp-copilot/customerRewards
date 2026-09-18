package my.customer.rewards.repository;

import my.customer.rewards.domain.PurchaseTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    List<PurchaseTransaction> findByCustomerUsernameOrderByTransactionDateAscIdAsc(String username);

    Page<PurchaseTransaction> findByCustomerUsernameOrderByTransactionDateAscIdAsc(
            String username, Pageable pageable);

    Optional<PurchaseTransaction> findByCustomerUsernameAndIdempotencyKey(
            String username, String idempotencyKey);

    List<PurchaseTransaction> findByCustomerUsernameAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            String username, LocalDate from, LocalDate to);

    @Query("select t from PurchaseTransaction t where t.customer.username = :username " +
            "and t.transactionDate >= :from and t.transactionDate < :to " +
            "order by t.transactionDate asc, t.id asc")
    List<PurchaseTransaction> findForMonth(@Param("username") String username,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}
