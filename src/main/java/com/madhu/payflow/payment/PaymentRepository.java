package com.madhu.payflow.payment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRequestKey(String requestKey);
    List<Payment> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(Long fromId, Long toId);
}
