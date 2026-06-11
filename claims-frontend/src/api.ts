import type { ClaimFormData, PolicyInfo, ClaimSubmitResponse } from './types';

const MOCK_POLICIES: Record<string, PolicyInfo> = {
  'HV-2024-001234': {
    policyNumber: 'HV-2024-001234',
    holderName: 'Maria Mustermann',
    address: 'Musterstraße 42, 80331 München',
    coverageAmount: 50000,
    validUntil: '2026-12-31',
  },
  'HV-2023-005678': {
    policyNumber: 'HV-2023-005678',
    holderName: 'Hans Schmidt',
    address: 'Hauptstraße 7, 10115 Berlin',
    coverageAmount: 35000,
    validUntil: '2025-06-30',
  },
  'HV-2025-009999': {
    policyNumber: 'HV-2025-009999',
    holderName: 'Erika Meier',
    address: 'Gartenweg 3, 50667 Köln',
    coverageAmount: 75000,
    validUntil: '2027-03-15',
  },
};

export async function lookupPolicy(policyNumber: string): Promise<PolicyInfo | null> {
  await delay(600);
  return MOCK_POLICIES[policyNumber.trim().toUpperCase()] ?? null;
}

export async function submitClaim(data: ClaimFormData): Promise<ClaimSubmitResponse> {
  await delay(1200);
  const claimNumber = `SCH-${new Date().getFullYear()}-${Math.floor(100000 + Math.random() * 900000)}`;
  return {
    claimNumber,
    status: 'RECEIVED',
    message: `Ihr Schaden wurde erfolgreich gemeldet. Aktenzeichen: ${claimNumber}`,
  };
}

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
