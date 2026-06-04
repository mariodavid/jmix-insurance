# Policy Module — jmix-insurance

The **Policy Module** manages persistent insurance policies. It generates policy numbers, computes coverage dates, and publishes lifecycle events to downstream modules like Accounting.

---

## 1. Module Structure

```
policy/
├── policy-api/          ← Public interfaces, DTOs, and lifecycle events (No JPA)
├── policy-api-starter/  ← Spring Boot auto-configuration for the API
├── policy-core/         ← JPA entities, service implementations, and Liquibase
├── policy-core-starter/ ← Spring Boot auto-configuration for the Core
├── policy-ui/           ← Vaadin and Jmix Flow UI view controllers
└── policy-ui-starter/   ← Spring Boot auto-configuration for the UI
```

---

## 2. Database Model

- **Entity**: `Policy` (mapped to table `POLICY_POLICY`)
- **Key Attributes**:
  - `policyNo` (unique business key, generated in sequence as `HC-YYYY-NNNNNN`)
  - `partnerNo` (string value reference to the customer)
  - `insuranceProduct` (enum for insurance product type)
  - `coverageStart` (date when coverage begins)
  - `coverageEnd` (calculated automatically as `coverageStart + 1 year`)
  - `premium` & `paymentFrequency` (payment interval)

---

## 3. Public API Contract (`policy-api`)

Other modules must interact with this module by importing `policy-api` and utilizing the public `PolicyService` interface, DTOs, and events.

### 3.1. Public Services
The module exposes the `PolicyService` interface (`com.insurance.policy.api.service.PolicyService`):

```java
public interface PolicyService {
    /** 
     * Creates and saves a persistent Policy. 
     * Computes coverage dates, generates policyNo, and publishes the PolicyCreatedEvent.
     */
    PolicyDto createPolicy(CreatePolicyRequestDto request);

    /** Loads policy details by its technical UUID identifier */
    PolicyDto findPolicyById(java.util.UUID id);
}
```

### 3.2. Events Published
When a policy is successfully created, the core module publishes a synchronous Spring **Application Event**:
- **Event**: `PolicyCreatedEvent` (`com.insurance.policy.api.event.PolicyCreatedEvent`)
- **Payload**: Contains the `PolicyDto` detail.
- **Transaction Scope**: Synchronous. Any exceptions in listeners (e.g., in the Account Module) will automatically trigger a database rollback of the policy creation.

---

## 4. Integration Example

To listen for policy creations in downstream modules (e.g., `account-core`):

```java
import com.insurance.policy.api.event.PolicyCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PolicyCreatedListener {

    @EventListener
    public void onPolicyCreated(PolicyCreatedEvent event) {
        PolicyDto policy = event.getPolicy();
        // Respond to policy creation (e.g., generate a ledger account)
    }
}
```

---

## 5. Development Guidelines & Rules

1. **Coverage Period Invariant**: The `coverageEnd` property must always be computed in the backend (`PolicyServiceCore`) as exactly 1 year after `coverageStart`.
2. **Synchronous Events**: Keep transactional safety in mind. Since `PolicyCreatedEvent` is synchronous, do not block the thread or perform extremely slow external network calls within the listeners.
3. **No Direct Core Dependencies**: Never import classes from `quote-core`, `partner-core`, or `account-core` into this module.
