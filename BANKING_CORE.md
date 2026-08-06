# Banking Core

A framework-free Java 17 banking core demonstrating business rules before infrastructure.

## Capabilities and invariants

- Open an account with one currency and a non-negative opening balance.
- Deposit and withdraw only positive amounts.
- Apply independently configurable withdrawal and transfer fees.
- Transfer atomically in memory: source, destination, and history remain unchanged when validation
  fails. The fee is charged to the source and the destination receives the requested amount.
- Never permit an account balance below zero.
- Reject cross-currency operations (foreign exchange is not silently assumed).
- Keep immutable transaction-history views with deterministic identifier/time generation.
- Notify zero or more outbound notification observers after a successful operation.

## Structure

```text
src/main/java/dev/studyforge/banking/
├── domain/       # Money, Account, Transaction, fee policy, domain errors
└── application/  # Deposit/Withdrawal/Transfer use cases and NotificationPort
```

The domain imports no Spring, PostgreSQL, HTTP, or infrastructure types. See
[`docs/adr/0001-pure-java-banking-core.md`](docs/adr/0001-pure-java-banking-core.md) for the selected
patterns, rejected abstractions, and trade-offs.

## Run tests

Requires JDK 17+ and Maven 3.8+:

```bash
mvn test
```
