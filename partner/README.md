# Partner Module — jmix-insurance

The **Partner Module** manages customer master data (business partners). It serves as the primary system of record for all partner numbers, first names, and last names, and owns the generation of unique partner identifier keys.

---

## 1. Module Structure

```
partner/
├── partner-api/          ← Public interfaces, Jmix DTOs, and events (No JPA)
├── partner-api-starter/  ← Spring Boot auto-configuration for the API
├── partner-core/         ← JPA entities, service implementations, and Liquibase
├── partner-core-starter/ ← Spring Boot auto-configuration for the Core
├── partner-ui/           ← Vaadin and Jmix Flow UI view controllers
└── partner-ui-starter/   ← Spring Boot auto-configuration for the UI
```

---

## 2. Database Model

- **Entity**: `Partner` (mapped to table `PARTNER_PARTNER`)
- **Key Attributes**:
  - `partnerNo` (unique, generated business key in the format `PT-NNNNN`)
  - `firstName`
  - `lastName`
  - Standard audit and soft-delete properties (`createdBy`, `createdDate`, etc.)

---

## 3. Public API Contract (`partner-api`)

Other modules must **only** interact with this module by importing `partner-api` and utilizing the public `PartnerService` interface and `PartnerDto`.

### 3.1. DTO Definition
`PartnerDto` is a Jmix DTO (non-persistent entity) decorated with Jmix metadata (`@JmixEntity(name = "partner_api_PartnerDto")`). It does not contain JPA mappings.

### 3.2. Public Services
The module exposes the `PartnerService` interface (`com.insurance.partner.api.service.PartnerService`):

```java
public interface PartnerService {
    /** Searches partners by wildcard search term */
    List<PartnerDto> findPartners(String search, int limit, int offset);

    /** Loads a single partner by its business key (e.g. "PT-10001") */
    PartnerDto getPartner(String partnerNo);

    /** Creates or updates a partner record */
    PartnerDto savePartner(PartnerDto partnerDto);
}
```

---

## 4. Integration Example

To retrieve and bind partner names in a foreign UI controller (e.g., `QuoteDetailView.java`):

```java
@Autowired
private PartnerService partnerService;

// Lazy loading a combo box of partners
private void loadPartners() {
    List<PartnerDto> partners = partnerService.findPartners("", 50, 0);
    partnerComboBox.setItems(partners);
}
```

---

## 5. Development Guidelines & Rules

1. **JPA Encaspulation**: Do **not** expose the persistent JPA `Partner` entity in the API module or return it to foreign modules. Always map it to `PartnerDto` first.
2. **Business Key Verification**: When creating a partner without an explicit `partnerNo`, the implementation in `partner-core` (`PartnerServiceImpl`) must generate a new number in sequence starting at `PT-10001`.
3. **No Foreign Core Imports**: Never import classes from `quote-core`, `policy-core`, or `account-core` into this module.
