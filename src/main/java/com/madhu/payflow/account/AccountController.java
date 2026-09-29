package com.madhu.payflow.account;

import com.madhu.payflow.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository accounts;
    public AccountController(AccountRepository accounts) { this.accounts = accounts; }

    public record CreateAccount(@NotBlank String name, @Pattern(regexp = "SGD", message = "Only SGD is supported in V1") String currency,
                                @jakarta.validation.constraints.DecimalMin("0.00") @jakarta.validation.constraints.Digits(integer = 10, fraction = 2) java.math.BigDecimal openingBalance) {}
    public record AccountView(Long id, String name, String currency, java.math.BigDecimal balance) {
        public static AccountView of(Account a) { return new AccountView(a.getId(), a.getName(), a.getCurrency(), a.getBalance()); }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountView create(@Valid @RequestBody CreateAccount request) {
        return AccountView.of(accounts.save(new Account(request.name().trim(), request.currency(),
                request.openingBalance() == null ? java.math.BigDecimal.ZERO.setScale(2) : request.openingBalance().setScale(2))));
    }

    @GetMapping("/{id}")
    public AccountView get(@PathVariable Long id) {
        return AccountView.of(accounts.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Account not found")));
    }
}
