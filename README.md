# StudyForge Java Curriculum

## About StudyForge
StudyForge is a framework-free Java curriculum organized as independent lessons and a small banking project.

## Prerequisites
Java 21, Maven Wrapper, and Git.

## Learning Path
Follow labs 01 through 10 in order. Each lab is a standalone Maven module.

## Labs
| Number | Title | Status | Prerequisite | Learning outcome | Path |
|---|---|---|---|---|---|
| 01 | Object Contracts | Active | Java basics | Implement value-object contracts | labs/01-object-contracts/README.md |
| 02 | OOP Payments | Active | 01 | Implement polymorphic payments | labs/02-oop-payments/README.md |
| 03 | Generics | Active | 02 | Implement reusable generic types | labs/03-generics/README.md |
| 04 | Collections | Active | 03 | Compare collection behavior | labs/04-collections/README.md |
| 05 | Streams and Optional | Active | 04 | Compose transaction reports | labs/05-streams-optional/README.md |
| 06 | Notification Composition | Active | 05 | Compose notification ports | labs/06-notification-composition/README.md |
| 07 | SOLID Transfer | Active | 06 | Apply SOLID boundaries | labs/07-solid-transfer/README.md |
| 08 | Concurrency | Active | 07 | Test concurrent state | labs/08-concurrency/README.md |
| 09 | JVM Runtime | Active | 08 | Explain runtime diagnostics | labs/09-jvm-runtime/README.md |
| 10 | Testing | Active | 09 | Design isolated tests | labs/10-testing/README.md |

## Projects
- banking-core - Active, framework-free application core (projects/banking-core/README.md).
- banking-cli - Active, thin command-line composition root (projects/banking-cli/README.md).

## Examples
- 00-object-model - Java object model examples (examples/00-object-model/README.md).
- backend-operations - Planned HTTP operations example (examples/backend-operations/README.md).

## Repository Structure
See docs/README.md for documentation indexes, templates, and ADRs.

## Build and Test
From the repository root:

    ./mvnw -B -ntp verify
    ./scripts/validate-curriculum.ps1

## Adding a New Lesson
Run ./scripts/new-lab.ps1 -Number 11 -Slug exceptions -Title Exceptions-and-Error-Handling -Prerequisite 10-testing.

## Planned Content
Spring Boot APIs, persistence adapters, and deployment lessons are planned and are not existing modules.

## Contributing
Keep lessons independent, use English documentation, add deterministic tests, and run validation before opening a pull request.
