package com.madhu.payflow.account;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String currency;
    private BigDecimal balance = BigDecimal.ZERO.setScale(2);
    @Version
    private long version;

    protected Account() {}
    public Account(String name, String currency, BigDecimal openingBalance) {
        this.name = name;
        this.currency = currency;
        this.balance = openingBalance;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public void credit(BigDecimal amount) { balance = balance.add(amount); }
    public void debit(BigDecimal amount) { balance = balance.subtract(amount); }
}
