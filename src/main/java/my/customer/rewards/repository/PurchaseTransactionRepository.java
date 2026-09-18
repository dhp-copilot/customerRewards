package my.customer.rewards.repository;

import my.customer.rewards.domain.PurchaseTransaction;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    List<PurchaseTransaction> findByCustomerUsernameOrderByTransactionDateAscIdAsc(String username);

    List<PurchaseTransaction> findByCustomerUsernameAndTransactionDateBetweenOrderByTransactionDateAscIdAsc(
            String username, LocalDate from, LocalDate to);

    @Query("select t from PurchaseTransaction t where t.customer.username = :username " +
            "and t.transactionDate >= :from and t.transactionDate < :to " +
            "order by t.transactionDate asc, t.id asc")
    List<PurchaseTransaction> findForMonth(@Param("username") String username,
                                            @Param("from") LocalDate from,
                                            @Param("to") LocalDate to);
}
