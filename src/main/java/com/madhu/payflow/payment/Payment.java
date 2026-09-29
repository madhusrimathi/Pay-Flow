package com.madhu.payflow.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "request_key"))
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "request_key", nullable = false, updatable = false)
    private String requestKey;
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
    private Instant createdAt;

    protected Payment() {}
    public Payment(String requestKey, Long fromAccountId, Long toAccountId, BigDecimal amount) {
        this.requestKey = requestKey;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getRequestKey() { return requestKey; }
    public Long getFromAccountId() { return fromAccountId; }
    public Long getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
