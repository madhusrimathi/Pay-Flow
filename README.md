# PayFlow

A learning project that simulates account-to-account payments in SGD. It is **not a real payment service** and holds no real money.

## Current features

- Create accounts with a demo opening balance and read balances.
- Transfer funds atomically between two accounts.
- Record two signed ledger entries for every successful transfer; their sum is zero.
- Reject overdrafts, invalid amounts and transfers to the same account.
- Reuse an `Idempotency-Key` safely for the same transfer, or reject a conflicting reuse.
- Lock accounts in a consistent order during transfers to prevent concurrent overspending.
- View account payment history and a payment's ledger entries.

Opening balances are **demo seed values**, not funded ledger transactions. The ledger covers transfers only. Authentication, a funding ledger, migrations, reconciliation and external payment rails are future work.

## Run locally

Requires Java 17+, Maven 3.6.3+ and Docker Compose.

```bash
docker compose up -d db
mvn spring-boot:run
```

The database uses local demo credentials from `compose.yaml`; override `DB_URL`, `DB_USER` and `DB_PASSWORD` in another environment. Run tests with `mvn test` (tests use H2 and need no Docker).

## Try a transfer

```bash
curl -s -X POST http://localhost:8080/api/accounts -H 'Content-Type: application/json' -d '{"name":"Alice","currency":"SGD","openingBalance":100.00}'
curl -s -X POST http://localhost:8080/api/accounts -H 'Content-Type: application/json' -d '{"name":"Bob","currency":"SGD","openingBalance":0.00}'
curl -s -X POST http://localhost:8080/api/payments -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-transfer-1' -d '{"fromAccountId":1,"toAccountId":2,"amount":30.00}'
curl -s http://localhost:8080/api/accounts/1
curl -s http://localhost:8080/api/accounts/1/payments
curl -s http://localhost:8080/api/payments/1/ledger
```

Expected balances: Alice SGD 70.00, Bob SGD 30.00. Ledger entries: Alice -30.00 and Bob +30.00. Repeating the same payment request and key returns the same payment without moving funds again. IDs may differ if the database already has records.

## How it works

`AccountController → AccountRepository → PostgreSQL` handles accounts. `PaymentController → PaymentService` validates a transfer, locks both accounts, updates balances, saves the payment and writes balanced ledger entries in one database transaction. A failure rolls the transaction back.

## Next milestones

1. Replace demo opening balances with balanced funding entries and reconciliation.
2. Add Flyway migrations and API level integration tests.
3. Add authentication, account ownership and authorization before any deployment.
4. Add an outbox for events, then experiment with Kafka and fraud checks.

Study references: [Spring Data JPA guide](https://spring.io/guides/gs/accessing-data-jpa/) and [Spring transactions guide](https://spring.io/guides/gs/managing-transactions/). This implementation is original learning code; the referenced guides explain underlying Spring concepts.
