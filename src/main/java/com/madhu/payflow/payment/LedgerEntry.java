package com.madhu.payflow.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class LedgerEntry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false)
    private Long paymentId;
    @Column(nullable = false, updatable = false)
    private Long accountId;
    @Column(nullable = false, updatable = false)
    private BigDecimal amount;
    protected LedgerEntry() {}
    public LedgerEntry(Long paymentId, Long accountId, BigDecimal amount) {
        this.paymentId = paymentId;
        this.accountId = accountId;
        this.amount = amount;
    }
    public Long getId() { return id; }
    public Long getPaymentId() { return paymentId; }
    public Long getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
}
