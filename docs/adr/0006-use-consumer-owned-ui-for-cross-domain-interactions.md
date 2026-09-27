# ADR-0006: Use Consumer-Owned UI for Cross-Domain Interactions

- **Status:** Accepted
- **Decision date:** Before 2026-05-30 (earliest repository evidence)
- **Recorded:** 2026-06-16
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md), [ADR-0005: Store Cross-Domain References as Local Domain Representations](0005-store-cross-domain-references-as-local-domain-representations.md)

## Context

Quote creation needs Partner data. A user must search for a policyholder, select one, see the
relevant identifying information, and store Quote's local Partner representation. Partner owns the
source data, but the selection is part of the Quote creation process.

The cross-domain API boundary from ADR-0002 prevents Quote UI from using the Partner entity or
depending on Partner Core. It does not decide which domain owns the cross-domain presentation. The
Partner domain could provide reusable UI, the application could assemble the interaction centrally,
or Quote could obtain Partner data through the public API and render the complete interaction
itself.

Avoiding every dependency between domains is not the goal. Quote has a legitimate directed
dependency on Partner because an offer needs a policyholder. The boundary must allow that business
dependency while preventing Quote from inheriting Partner's persistence and UI implementation.

## Decision Drivers

- Let the domain that owns the user interaction determine its layout and behavior.
- Preserve the legitimate dependency direction from Quote to Partner.
- Keep Quote independent of Partner Core entities, repositories, views, and fragments.
- Use Jmix data binding in the consuming UI without exposing a foreign persistent entity.
- Support server-side search, filtering, pagination, and lazy item loading for Partner selection.
- Let Quote decide which Partner values become part of its local representation.
- Keep the interaction viable if Partner later becomes a remote service.

## Considered Options

### 1. Reuse a concrete Partner UI implementation

Quote could depend on `partner-ui` and embed a Partner view, fragment, or other concrete component.

This option could reuse Partner's existing presentation. It would also couple Quote to Partner's UI
implementation, component lifecycle, view structure, security assumptions, and transitive Core
dependency. Partner UI changes could unexpectedly alter the Quote process.

### 2. Assemble the interaction centrally in `webapp`

The runnable application could connect the Quote and Partner UI modules and own the selection
interaction.

This avoids a direct Quote-to-Partner dependency but moves behavior belonging to quote creation
into the composition root. The `webapp` would accumulate domain-specific view orchestration and
would need to change whenever the Quote use case changes.

### 3. Let Partner provide a UI extension through an SPI

Partner could define or implement an extension that contributes the selection UI to Quote.

This fits content whose presentation remains owned by Partner. It is less suitable for the Quote
creation process because Quote needs to control the fields, selection behavior, validation, and
translation into its local model. It would give Partner influence over an interaction owned by
Quote.

### 4. Let Quote own the UI and consume Partner's API

Quote can depend on `partner-api`, request `PartnerDto` instances through `PartnerService`, and
build the selection and presentation inside Quote UI.

This retains the intended dependency direction while keeping presentation ownership with the
consumer of the data.

## Decision

Use **consumer-owned UI composition** when a cross-domain interaction belongs to the consuming
domain's use case.

For policyholder selection, Quote UI owns:

- the `EntityComboBox` and surrounding fields;
- the search and pagination interaction;
- the way Partner attributes are presented;
- the reaction to selection and deselection;
- the mapping into Quote's local `QuotePartnerReference`;
- validation and read-only behavior inside the Quote process.

Partner owns the public data contract. `PartnerService` exposes lookup and paginated search
operations, and `PartnerDto` carries the values available to consumers. Quote UI depends on
`partner-api`, not on `partner-core` or `partner-ui`.

The dependency `quote-ui -> partner-api` is intentional. Domain separation does not require an
acyclic graph with no business dependencies; it requires dependencies to point to explicit public
contracts instead of foreign implementations.

## Jmix Boundary Types

`PartnerDto` is a Jmix DTO entity rather than a plain persistence-neutral Java record. It remains
non-persistent, but Jmix metadata makes it directly usable by data-aware UI components such as
`EntityComboBox` and data containers. The consumer can therefore retain normal Jmix data binding
without receiving the Partner JPA entity.

The same principle applies to stable enums and other boundary types placed in API artifacts. A
consumer may use those types directly when they are intentionally part of the public contract. API
types are shared contracts; Core entities and UI implementation types are not.

Supporting consumer-owned UI has consequences for the service contract. Partner must expose the
query capabilities required by its consumers, including filtered and paginated search. Quote must
not bypass an insufficient API with a foreign JPQL query or direct table access. If the Quote use
case needs another Partner attribute or query capability, the Partner API must evolve explicitly.

## Consequences

### Intended benefits

- Quote controls an interaction that is part of the quote-creation process.
- The UI can be shaped specifically for Quote instead of inheriting Partner's general-purpose
  screens.
- The business dependency from Quote to Partner is visible in the Gradle and API dependency graph.
- Quote uses Jmix data binding without gaining access to Partner's persistent entity model.
- Partner can change its Core and UI implementations without forcing corresponding Quote UI
  changes, provided the public API remains compatible.
- Quote decides which Partner values it stores locally and how they are used in its workflow.
- A later remote Partner implementation can preserve the consumer-owned Quote UI.

### Accepted costs and limitations

- Quote implements and maintains its own representation of Partner selection.
- Presentation improvements in Partner UI do not automatically appear in Quote UI.
- Similar Partner information may be rendered differently or repeatedly in several consumers.
- Partner API must provide UI-relevant search, pagination, DTO metadata, and attributes without
  becoming a generic exposure of its complete entity model.
- Changes to the Partner API may require coordinated changes in consumers.
- A Jmix DTO entity intentionally couples the boundary type to Jmix metadata, even though it does
  not couple consumers to Partner persistence.
- Replacing the local service with a remote client still requires transport, error handling,
  latency handling, authentication, and API compatibility work.

## Migration When Partner Is Extracted

The ownership model remains valid if Partner becomes a separately deployed service: Quote still
owns the selection UI, and Partner still owns the data contract.

The in-process `PartnerService` implementation would be replaced or adapted by a remote client.
That migration must address network latency, partial failure, timeouts, paging semantics,
authentication, caching, and DTO serialization. The current API boundary gives that work an
explicit location, but it does not make a local Java service automatically suitable as a remote
protocol.

## Guardrails and Agent Guidance

- Use consumer-owned UI when the foreign data participates in an interaction owned by the host
  domain.
- Depend on the foreign domain's API artifact and use its services, DTOs, enums, and other public
  boundary types.
- Do not import foreign Core entities, repositories, view controllers, or fragments.
- Keep layout, component behavior, validation, and local state mapping in the consuming UI module.
- Extend the owning domain's API when the consumer needs additional query behavior; do not replace
  the API with foreign JPQL, metadata lookups, or SQL.
- Keep API DTOs small and use-case-oriented enough to avoid recreating the persistent entity as a
  public canonical model.
- Use a provider-owned UI contribution instead when the contributing domain must retain ownership
  of an optional section's content and presentation. That pattern is governed by a separate ADR.

## Evidence in the Repository

- [`QuoteDetailView`](../../quote/quote-ui/src/main/java/com/insurance/quote/ui/view/quote/QuoteDetailView.java) owns the Partner selection UI, loads `PartnerDto` instances, and writes the selected values into Quote.
- [`quote-detail-view.xml`](../../quote/quote-ui/src/main/resources/com/insurance/quote/ui/view/quote/quote-detail-view.xml) binds the Quote-owned controls to the local Quote model and the Partner DTO metadata type.
- [`PartnerService`](../../partner/partner-api/src/main/java/com/insurance/partner/api/service/PartnerService.java) provides lookup and paginated search for foreign consumers.
- [`PartnerDto`](../../partner/partner-api/src/main/java/com/insurance/partner/api/dto/PartnerDto.java) is a non-persistent Jmix DTO entity usable in consumer-side data binding.
- [`quote-ui.gradle`](../../quote/quote-ui/quote-ui.gradle) declares Quote UI's dependency on the Partner API starter without depending on Partner Core or Partner UI.
- [`UiCompositionBoundaryRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/ui/UiCompositionBoundaryRules.java) prevents domain UI modules from using foreign UI implementations.
- [`article-draft.md`](../article-draft.md#455-ui-integration) contrasts consumer-owned UI with provider-owned contribution.

## Follow-up Decision

A separate ADR defines provider-owned UI contributions through host-owned `*-ui-api` extension
contracts for cases where a domain contributes optional content to another domain's view.
