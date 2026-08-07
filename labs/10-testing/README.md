# 10 - Testing

## Overview
This independent Java 21 lesson focuses on executable specifications.

## Learning Objectives
- Implement isolated tests.
- Compare unit and concurrency tests.
- Explain failure evidence.

## Prerequisites
[Previous lesson](../09-jvm-runtime/README.md)

## What You Will Build
A focused testing example local to this lesson.

## Concepts Covered
Test isolation, fixtures, concurrency, and failure diagnostics.

## Project Structure
- src/main/java: production code.
- src/test/java: acceptance tests.
- pom.xml: reactor module build.

## Running the Lab
From repository root: ./mvnw -B -ntp -pl labs/10-testing test

## Exercises
1. Implement a focused test.
2. Add an edge-case test.
3. Explain the observed behavior.

## Acceptance Criteria
1. Tests are deterministic.
2. Failure messages identify the invariant.
3. The module test command passes.

## Tests
Run ./mvnw -B -ntp -pl labs/10-testing test.

## Common Pitfalls
Do not share this lesson fixture with another module.

## Further Exploration
Repeat concurrency tests with fork isolation.

## Next Lesson
This is the final lesson in the current path.

