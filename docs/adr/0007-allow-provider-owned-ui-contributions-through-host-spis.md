# ADR-0007: Allow Provider-Owned UI Contributions Through Host-Defined SPIs

- **Status:** Accepted
- **Decision date:** 2026-06-07 (earliest repository evidence)
- **Recorded:** 2026-06-20
- **Type:** Retrospectively documented decision
- **Related:** [ADR-0002: Apply Asymmetric API/Core/UI Layering](0002-apply-asymmetric-api-core-ui-layering.md), [ADR-0006: Use Consumer-Owned UI for Cross-Domain Interactions](0006-use-consumer-owned-ui-for-cross-domain-interactions.md)

## Context

Some central detail views are natural integration points for information owned by surrounding
domains. A Policy detail view may show policyholder information from Partner and account information
from Accounting. A Partner detail view may show policies and financial information. More such
contributions can appear as the application grows.

The consumer-owned composition from ADR-0006 is appropriate when the host owns the complete
interaction. Applying it to every additional section would make the host load all foreign data and
implement every presentation itself. Policy UI would acquire directed dependencies on Partner,
Account, and every future contributor. The central detail view would become the integration layer
for all surrounding domains.

Depending directly on foreign UI implementations would create an even stronger graph. Policy UI
would need to know concrete fragments from all providers, and installing or removing one provider
would require changing the host. The intended modularity needs the dependency direction to be
inverted for optional, provider-owned content.

## Decision Drivers

- Keep central detail views open for optional information from surrounding domains.
- Prevent host UI modules from accumulating dependencies on every contributing UI implementation.
- Let the domain that owns the information also own its presentation and lookup behavior.
- Keep persistent entities and Core implementations out of cross-module UI contracts.
- Allow providers to be installed, removed, or added without changing the host controller.
- Reuse Jmix fragments as self-contained UI building blocks.
- Provide a microfrontend-like ownership model without introducing separate frontend deployments.

## Considered Options

### 1. Let the host know all provider implementations

`PolicyDetailView` could depend on Account UI and Partner UI, instantiate their fragments, and pass
the current Policy to them.

This option is explicit and easy to trace for a small number of integrations. It creates a growing
dependency graph from the central host to all surrounding modules. Adding a provider requires a
host change, and exposing the Policy entity would also create a foreign Core dependency.

### 2. Use consumer-owned UI for every section

Policy UI could call Account and Partner APIs, receive DTOs, and render all additional information
itself, as Quote does for Partner selection in ADR-0006.

This is suitable when Policy owns the interaction. It is less suitable for optional sections whose
data, terminology, and presentation should remain with Account or Partner. Policy UI would absorb
knowledge and maintenance work from every provider.

### 3. Assemble concrete sections in `webapp`

The composition root could connect host views and provider fragments centrally.

This keeps direct provider knowledge out of Policy UI but moves detailed UI integration into the
application assembly. The `webapp` would require domain-specific controller customizations and
would become a second owner of the participating views.

### 4. Publish a host-owned extension contract

The host domain can publish a small Service Provider Interface in `*-ui-api`. Provider UI modules
implement the contract, load their own data, and return their own Jmix fragments. The host discovers
all installed implementations without knowing their classes.

This inverts the implementation dependencies while retaining ownership on both sides: the host
owns the extension point, and the provider owns the contributed content.

## Decision

Use **host-defined SPIs with provider-owned UI contributions** for optional cross-domain sections
on central detail views.

The host domain publishes:

- a section interface in its `*-ui-api` artifact;
- a small context contract containing the identifiers needed by providers;
- the location and outer presentation into which contributions are inserted.

Provider UI modules implement the section interface as Spring beans. Each provider uses the
identifiers from the host context to load its own local representation through its own services and
creates a domain-owned Jmix fragment for the resulting content.

The host discovers installed contributions as an ordered collection of Spring beans. It does not
reference provider classes and does not require a contribution to be present. Adding another
provider therefore adds a dependency from the provider UI module to the host's UI API, not from the
host UI implementation to the provider.

## Contract Ownership

The extension contract belongs to the host because the host owns the detail view and decides where
and in what form extension is allowed. For example, Policy owns `PolicySection` and
`PolicyViewContext`; Partner owns `PartnerSection` and `PartnerViewContext`.

The provider owns the contribution because it owns the additional information. Account decides how
to retrieve and present account information for a Policy. Partner decides how to present the
policyholder. The host does not reproduce those domain-specific queries or UI components.

The shared `ui-sections` library contains only the generic `ViewSection<C>` shape. It does not define
domain-specific slots or a common cross-domain data model.

## Identifier-Only Host Context

The host context contains the technical or business identifiers required for a provider lookup and,
where useful, simple boundary values. It must not contain the host's persistent entity.

A provider uses values such as a Policy ID, policy number, or partner number to locate its own local
representation and load the information it owns. Passing a Policy entity to Account UI would make
Account depend on Policy Core and would turn the SPI into another route around the bounded-context
boundary.

The context is not intended to become a copy of the host entity. New fields are added only when they
are stable parts of the extension contract and avoid an otherwise unjustified lookup.

## Jmix Implementation

The pattern uses two collaborating objects:

- A section adapter is an ordered Spring bean implementing the host SPI.
- The adapter creates the provider's Jmix fragment through the `Fragments` factory and supplies the
  host identifiers.

The adapter participates in Spring discovery, while the fragment remains a normal Jmix UI building
block with its XML descriptor, data components, injected services, and event handlers. This is an
implementation of the ownership model rather than a requirement that provider fragments use a
particular visual layout.

The host controls the extension area and outer container. Providers supply their title, relative
order, content, data access, and interactions inside the contribution.

## Microfrontend-Like Scope

This pattern provides microfrontend-like ownership inside the modular monolith: different domains
own independently contributed pieces of a shared page, and the host does not compile against their
implementations.

It is not an independently deployed microfrontend architecture. Host and providers share the same
Spring application, Vaadin session, Jmix metadata, Java classpath, and release. A separately
deployed service cannot contribute the same Java fragment remotely.

If a provider is extracted, its contribution must be replaced deliberately, for example with
consumer-owned UI backed by a remote API, navigation to the provider's UI, a Web Component, or an
independently deployed frontend integration.

## Consequences

### Intended benefits

- Central detail views do not acquire dependencies on every surrounding UI implementation.
- A provider can add or remove an optional section without changing the host controller.
- The provider retains ownership of its data retrieval, terminology, presentation, and
  interaction.
- Host and provider exchange identifiers instead of persistent entities.
- The dependency graph points from provider UI to a narrow host UI API.
- Multiple providers can contribute to the same host through one consistent mechanism.
- The repeated pattern gives developers and coding agents a recognizable place for new
  cross-domain UI sections.

### Accepted costs and limitations

- Every extensible host needs a small `*-ui-api` artifact or contract package.
- UI behavior is distributed between the host, section adapters, and fragments, making navigation
  less direct than explicit construction.
- Contribution ordering requires a convention and can produce conflicts as the number of providers
  grows.
- Host-context changes are API changes for every provider.
- A provider must perform its own lookup from the supplied identifiers.
- The complete page still has runtime coupling: a slow or failing provider can affect the shared
  view unless additional isolation is introduced.
- The mechanism requires one Spring/Jmix/Vaadin runtime and does not cross a service boundary.
- Host and providers are independently owned in source, not independently deployable in the
  browser.

## Guardrails and Agent Guidance

- Define the extension interface and context in the host domain's `*-ui-api` artifact.
- Keep the context free of persistent entities and foreign Core types.
- Pass only identifiers and simple stable values required by provider lookups.
- Implement contributions in the contributing domain's UI module.
- Keep provider-specific services, fragments, XML descriptors, messages, and interaction logic in
  that provider module.
- Register section adapters as ordered Spring beans and keep the host independent of their concrete
  classes.
- Do not add a provider UI implementation dependency to the host to make fragment creation more
  convenient.
- Use consumer-owned UI instead when the host owns the complete cross-domain interaction.
- Treat a move across a process boundary as a new UI integration decision rather than attempting to
  invoke a Java fragment remotely.

## Evidence in the Repository

- [`ViewSection`](../../ui-sections/src/main/java/com/insurance/ui/section/ViewSection.java) defines the generic contribution shape.
- [`PolicySection`](../../policy/policy-ui-api/src/main/java/com/insurance/policy/ui/api/PolicySection.java) and [`PolicyViewContext`](../../policy/policy-ui-api/src/main/java/com/insurance/policy/ui/api/PolicyViewContext.java) form the Policy host contract.
- [`PolicyDetailView`](../../policy/policy-ui/src/main/java/com/insurance/policy/ui/view/policy/PolicyDetailView.java) discovers and renders installed Policy sections without referencing provider implementations.
- [`PolicyHolderSection`](../../partner/partner-ui/src/main/java/com/insurance/partner/ui/view/policy/PolicyHolderSection.java) adapts Partner's `PolicyHolderFragment` to the Policy SPI.
- [`PolicyAccountBalanceSection`](../../account/account-ui/src/main/java/com/insurance/account/ui/view/policy/PolicyAccountBalanceSection.java) contributes Account-owned content to the Policy detail view.
- [`PartnerSection`](../../partner/partner-ui-api/src/main/java/com/insurance/partner/ui/api/PartnerSection.java) provides the equivalent extension point for Partner detail views.
- [`PartnerPoliciesSection`](../../policy/policy-ui/src/main/java/com/insurance/policy/ui/view/partner/PartnerPoliciesSection.java) and [`PartnerAccountSection`](../../account/account-ui/src/main/java/com/insurance/account/ui/view/partner/PartnerAccountSection.java) contribute provider-owned Partner sections.
- [`UiSectionContributionRules`](../../test-support/src/main/java/com/insurance/common/test_support/architecture/rules/ui/UiSectionContributionRules.java) checks placement, Spring registration, ordering, host-boundary dependencies, and message keys.
- [`article-draft.md`](../article-draft.md#455-ui-integration) contrasts provider-owned contribution with consumer-owned composition.

## Follow-up Decisions

Separate ADRs should document:

- the testing strategy for host views with optional provider contributions;
- the Jmix-specific architecture rules that validate UI API boundaries and contribution
  conventions;
- the replacement mechanism when a contributing domain is deployed independently.
