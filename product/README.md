# Product Module — jmix-insurance

The **Product Module** owns insurance product configuration. It is intentionally lightweight:
products, product types, variants, and payment frequencies are modeled as Java enums in the API
module, not as persistent database entities.

## Module Structure

```
product/
├── product-api/          ← Public enum DTOs and product calculation logic
├── product-api-starter/  ← Spring Boot auto-configuration for the API
├── product-core/         ← ProductConfiguration and module wiring
├── product-core-starter/ ← Spring Boot auto-configuration for the Core
├── product-ui/           ← Reserved UI add-on surface
└── product-ui-starter/   ← Spring Boot auto-configuration for the UI
```

## Public Contract

Foreign modules should depend on `product-api` and use:

- `InsuranceProduct`
- `ProductType`
- `ProductVariant`
- `PaymentFrequency`

`InsuranceProduct.calculatePremium(squareMeters)` is the canonical premium calculation entry point.

## Rules

- Do not introduce JPA entities or Liquibase changelogs unless product configuration is deliberately
  moved from enum-based to database-backed.
- Do not couple product code to partner, quote, policy, or account core classes.
- Keep enum ids stable because they are persisted by other modules.
