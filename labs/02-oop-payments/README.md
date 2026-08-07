# 01 - Object Contracts

## Overview
This independent Java 21 lesson explores value-object contracts.

## Learning Objectives
- Implement equality and hashing.
- Compare mutable and immutable state.
- Explain contract failures with tests.

## Prerequisites
Java 21 and basic object-oriented programming.

## What You Will Build
A focused set of value-object examples local to this lesson.

## Concepts Covered
Equality, hashing, immutability, and mutability hazards.

## Project Structure
- src/main/java: production examples.
- src/test/java: acceptance tests.
- pom.xml: reactor module build.

## Running the Lab
From repository root: ./mvnw -B -ntp -pl labs/01-object-contracts test

## Exercises
1. Implement a value-object contract.
2. Add an edge-case test.
3. Explain the observed behavior.

## Acceptance Criteria
1. Equality and hash code agree.
2. Tests are deterministic.
3. The module test command passes.

## Tests
Run ./mvnw -B -ntp -pl labs/01-object-contracts test.

## Common Pitfalls
Do not share this lesson fixture with another module.

## Further Exploration
Compare a record with a mutable class.

## Next Lesson
[Next lesson](../02-oop-payments/README.md)

