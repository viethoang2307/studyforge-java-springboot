# banking-core

## Overview
Framework-free banking application core.

## Architecture
Domain model, application ports, services, and in-memory adapters.

## Use Cases
Open accounts, deposit, withdraw, transfer, and query history.

## Prerequisites
Java 21 and the repository Maven Wrapper.

## Configuration
No external configuration; adapters are injected.

## Run and Test
./mvnw -B -ntp -pl projects/banking-core test

## Module Dependencies
No project dependencies.

## Decisions
Transactions are committed atomically; notifications run after commit.

## Roadmap
Database and HTTP adapters are planned.

