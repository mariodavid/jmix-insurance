# Payment Management for Claims

## Description

As a claims handler, I want to record payments for a claim so that the insured party receives their compensation and the accounting system reflects the payout.

When a claim has an approved reserve, I need to be able to create one or more payments against that claim. Each payment reduces the remaining reserve. The accounting module must be notified so that the financial records stay in sync.

## Functional Requirements

- A claims handler can create a payment for a claim that has an approved reserve
- A payment has: amount, payment date, payment reference, and a comment
- The total of all payments for a claim must not exceed the current reserve
- Recording a payment creates a debit document in the accounting module (similar to how policy creation creates account documents)
- The claim detail view shows a list of all payments
- The claim detail view shows the remaining reserve (current reserve minus total payments)
- A payment cannot be edited or deleted after creation

## Business Rules

- Only claims with status IN_PROGRESS and an approved reserve can receive payments
- Payment amount must be greater than zero
- If a payment would cause total payments to exceed the reserve, it must be rejected with a clear error message
- The accounting document is created in the same transaction as the payment

## UI Requirements

- "Add Payment" action on the claim detail view
- Payment dialog with fields for amount, date, reference, and comment
- Payment history grid on the claim detail view showing all payments
- Display of remaining reserve (calculated: current reserve - sum of payments)

## Open Questions

- Should partial payments be explicitly marked as partial vs. final?
- Should there be a notification when the reserve is fully exhausted?
