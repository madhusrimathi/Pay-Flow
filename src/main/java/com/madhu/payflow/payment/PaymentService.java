package com.madhu.payflow.payment;

import com.madhu.payflow.account.Account;
import com.madhu.payflow.account.AccountRepository;
import com.madhu.payflow.common.ApiException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final AccountRepository accounts;
    private final PaymentRepository payments;
    private final LedgerRepository ledger;

    public PaymentService(AccountRepository accounts, PaymentRepository payments, LedgerRepository ledger) {
        this.accounts = accounts;
        this.payments = payments;
        this.ledger = ledger;
    }

    @Transactional
    public Payment transfer(String key, Long fromId, Long toId, BigDecimal amount) {
        if (amount == null || amount.scale() > 2 || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be positive with at most two decimal places");
        if (fromId.equals(toId)) throw new ApiException(HttpStatus.BAD_REQUEST, "Accounts must differ");
        // Always lock in ID order to avoid opposing transfers deadlocking.
        Account first = lock(Math.min(fromId, toId));
        Account second = lock(Math.max(fromId, toId));
        Account from = fromId.equals(first.getId()) ? first : second;
        Account to = toId.equals(first.getId()) ? first : second;
        var prior = payments.findByRequestKey(key);
        if (prior.isPresent()) {
            Payment p = prior.get();
            if (!p.getFromAccountId().equals(fromId) || !p.getToAccountId().equals(toId)
                    || p.getAmount().compareTo(amount) != 0)
                throw new ApiException(HttpStatus.CONFLICT, "Key was used for a different transfer");
            return p;
        }
        if (!from.getCurrency().equals(to.getCurrency()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Currencies must match");
        if (from.getBalance().compareTo(amount) < 0)
            throw new ApiException(HttpStatus.CONFLICT, "Insufficient funds");
        from.debit(amount);
        to.credit(amount);
        Payment payment = payments.save(new Payment(key, fromId, toId, amount));
        ledger.saveAll(List.of(new LedgerEntry(payment.getId(), fromId, amount.negate()),
                new LedgerEntry(payment.getId(), toId, amount)));
        return payment;
    }

    private Account lock(Long id) {
        return accounts.findWithLockById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Payment> history(Long accountId) {
        if (!accounts.existsById(accountId)) throw new ApiException(HttpStatus.NOT_FOUND, "Account not found");
        return payments.findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> entries(Long paymentId) {
        if (!payments.existsById(paymentId)) throw new ApiException(HttpStatus.NOT_FOUND, "Payment not found");
        return ledger.findByPaymentIdOrderById(paymentId);
    }
}
