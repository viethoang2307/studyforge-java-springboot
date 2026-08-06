# ADR 0001: Pure Java banking core and selective use of patterns

- **Status:** Accepted
- **Date:** 2026-08-06

## Context

The first banking slice must enforce money and transfer rules without requiring Spring, HTTP,
or a database. It also needs extension points for fees and notifications, but premature layers and
one-interface-per-class would make a small model harder to understand.

## Decision

Use a small domain/application split:

- `domain` owns `Account`, exact `Money`, transactions, fees, and business exceptions.
- `application` owns use-case commands, `BankingCore`, and the outbound `NotificationPort`.
- `BankingCore` validates a transfer completely before either balance is changed. This gives the
  in-memory implementation all-or-nothing behavior; a future database adapter must additionally
  supply a database transaction and concurrency control.
- The transaction history is append-only from the public API and queries return immutable snapshots.
- Notifications run after the transaction is recorded. A notification failure does not report a
  completed financial operation as failed; an infrastructure adapter is responsible for logging,
  retry, or durable delivery.

### Patterns retained because they solve a present variation

1. **Strategy:** `FeeStrategy` changes fee policy without branching in the use case. `noFee` and a
   percentage policy are currently useful configurations.
2. **Factory:** `TransactionFactory` centralizes identifiers and time. Injecting `Clock` and an ID
   supplier makes audit metadata deterministic in tests.
3. **Builder:** `Account.Builder` makes the optional opening balance explicit while keeping required
   identity, holder, and currency mandatory.
4. **Observer:** multiple `NotificationPort` implementations can observe completed transactions.
   The core depends only on this outbound port, not email, Kafka, Spring, or HTTP.

### Patterns deliberately not introduced

- **Adapter:** the port defines the seam for a future email/Kafka/CLI adapter, but this module has no
  real external system to adapt yet. A fake adapter written only to display the pattern adds no value.
- **Decorator:** there is no cross-cutting behavior that currently needs dynamic composition. Retry,
  metrics, or logging decorators should be added only alongside real infrastructure and an explicit
  operational policy.

## Consequences

- The core compiles and tests with Java and JUnit only; framework and persistence choices remain
  replaceable.
- Fee and notification behavior can vary without changing financial rules.
- State is process-local and operations are not safe for concurrent access. Persistence, locking,
  idempotency, durable notification, and recovery are explicitly outside this slice rather than being
  simulated by unused abstractions.
- Ignoring observer exceptions is intentional at this boundary, but production adapters must make
  failures observable and durable where delivery guarantees are required.

## Review of abstractions

No account repository, transaction repository, generic service base class, command bus, or mapper was
introduced. Each would have only one implementation and would hide rather than isolate the current
rules. Revisit those abstractions when a real persistence or transport adapter is added.
