# ADR 0001: Pure Java banking core

- Status: Accepted
- Date: 2026-08-07
- Scope: projects/banking-core
- Decision: Keep the banking core framework-free, expose application ports, and use an in-memory unit of work for atomic tests.
- Consequences: The CLI composes dependencies explicitly; future database and HTTP adapters can be added without changing domain invariants.
- Superseded-by: None

