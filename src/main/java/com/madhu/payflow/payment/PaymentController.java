package com.madhu.payflow.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    public record Transfer(@NotNull Long fromAccountId, @NotNull Long toAccountId,
                           @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount) {}
    public record PaymentView(Long id, Long fromAccountId, Long toAccountId, BigDecimal amount, java.time.Instant createdAt) {
        static PaymentView of(Payment p) { return new PaymentView(p.getId(), p.getFromAccountId(), p.getToAccountId(), p.getAmount(), p.getCreatedAt()); }
    }
    public record EntryView(Long accountId, BigDecimal amount) {
        static EntryView of(LedgerEntry e) { return new EntryView(e.getAccountId(), e.getAmount()); }
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentView transfer(@RequestHeader("Idempotency-Key") String key, @Valid @RequestBody Transfer request) {
        if (key.isBlank() || key.length() > 100) throw new com.madhu.payflow.common.ApiException(HttpStatus.BAD_REQUEST, "Invalid Idempotency-Key");
        return PaymentView.of(service.transfer(key, request.fromAccountId(), request.toAccountId(), request.amount().setScale(2)));
    }

    @GetMapping("/accounts/{id}/payments")
    public List<PaymentView> history(@PathVariable Long id) {
        return service.history(id).stream().map(PaymentView::of).toList();
    }

    @GetMapping("/payments/{id}/ledger")
    public List<EntryView> ledger(@PathVariable Long id) {
        return service.entries(id).stream().map(EntryView::of).toList();
    }
}
