# Architecture Decision Records

This directory records architectural decisions that are not obvious from the current source tree. The architecture overview in [`../architecture.md`](../architecture.md) describes the system as it exists today; the ADRs explain why selected structures were chosen, which alternatives were considered, and which consequences are accepted.

## Index

| ADR | Status | Decision |
|---|---|---|
| [ADR-0001](0001-use-a-modular-monolith-built-from-jmix-add-ons.md) | Accepted | Use a modular monolith built from Jmix add-ons |
| [ADR-0002](0002-apply-asymmetric-api-core-ui-layering.md) | Accepted | Apply asymmetric API/Core/UI layering |
| [ADR-0003](0003-create-policy-and-accounting-records-atomically.md) | Accepted | Create Policy and accounting records atomically |
| [ADR-0004](0004-enforce-module-boundaries-with-architecture-tests.md) | Accepted | Enforce module boundaries with architecture tests |
| [ADR-0005](0005-store-cross-domain-references-as-local-domain-representations.md) | Accepted | Store cross-domain references as local domain representations |
| [ADR-0006](0006-use-consumer-owned-ui-for-cross-domain-interactions.md) | Accepted | Use consumer-owned UI for cross-domain interactions |
| [ADR-0007](0007-allow-provider-owned-ui-contributions-through-host-spis.md) | Accepted | Allow provider-owned UI contributions through host-defined SPIs |
| [ADR-0008](0008-align-test-scope-with-module-boundaries.md) | Accepted | Align test scope with module boundaries |
| [ADR-0009](0009-use-domain-owned-test-data-and-a-shared-test-dsl.md) | Accepted | Use domain-owned test data and a shared test DSL |
| [ADR-0010](0010-enforce-jmix-metadata-and-resource-conventions-in-architecture-tests.md) | Accepted | Enforce Jmix metadata and resource conventions in architecture tests |
| [ADR-0011](0011-keep-migrations-and-security-policies-domain-owned.md) | Accepted | Keep migrations and security policies domain-owned |

## Maintenance

ADRs are kept in the repository and reviewed with the code they affect. Accepted records are not rewritten to make a later decision look inevitable. A changed decision receives a new ADR that supersedes the previous one, and the architecture overview is updated to describe the resulting current state.

Coding agents should read the architecture overview before broad structural work and consult the relevant ADR before changing a documented boundary. An agent may draft a record, but the decision context, alternatives, and consequences require human confirmation.
