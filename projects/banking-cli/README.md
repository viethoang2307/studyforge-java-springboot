# banking-cli

## Overview
Thin CLI composition root for banking-core.

## Architecture
The CLI depends on banking-core and owns no duplicate domain model.

## Use Cases
Run the deterministic demo entry point.

## Prerequisites
Java 21.

## Configuration
None.

## Run and Test
./mvnw -B -ntp -pl projects/banking-cli test

## Module Dependencies
projects/banking-core.

## Decisions
Keep framework-free composition at the edge.

## Roadmap
Interactive commands are planned.

