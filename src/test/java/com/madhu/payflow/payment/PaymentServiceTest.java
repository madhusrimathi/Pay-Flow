package com.madhu.payflow.payment;

import com.madhu.payflow.account.Account;
import com.madhu.payflow.account.AccountRepository;
import com.madhu.payflow.common.ApiException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {
    @Autowired AccountRepository accounts;
    @Autowired PaymentService service;

    @Test
    void transferIsBalancedAndDuplicateDoesNotPayTwice() {
        Account alice = accounts.save(new Account("Alice", "SGD", new BigDecimal("100.00")));
        Account bob = accounts.save(new Account("Bob", "SGD", BigDecimal.ZERO.setScale(2)));
        Payment p = service.transfer("test-key-1", alice.getId(), bob.getId(), new BigDecimal("30.00"));
        Payment retry = service.transfer("test-key-1", alice.getId(), bob.getId(), new BigDecimal("30.00"));
        assertEquals(p.getId(), retry.getId());
        assertEquals(0, accounts.findById(alice.getId()).orElseThrow().getBalance().compareTo(new BigDecimal("70.00")));
        assertEquals(0, accounts.findById(bob.getId()).orElseThrow().getBalance().compareTo(new BigDecimal("30.00")));
        var entries = service.entries(p.getId());
        assertEquals(2, entries.size());
        assertEquals(0, entries.get(0).getAmount().add(entries.get(1).getAmount()).compareTo(BigDecimal.ZERO));
        assertEquals(1, service.history(alice.getId()).size());
    }

    @Test
    void rejectsOverdraftWithoutWritingPayment() {
        Account alice = accounts.save(new Account("Alice", "SGD", new BigDecimal("10.00")));
        Account bob = accounts.save(new Account("Bob", "SGD", BigDecimal.ZERO.setScale(2)));
        assertThrows(ApiException.class, () -> service.transfer("test-key-2", alice.getId(), bob.getId(), new BigDecimal("11.00")));
        assertEquals(0, accounts.findById(alice.getId()).orElseThrow().getBalance().compareTo(new BigDecimal("10.00")));
        assertTrue(service.history(alice.getId()).isEmpty());
    }
}
