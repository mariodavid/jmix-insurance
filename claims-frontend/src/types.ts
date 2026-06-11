export type DamageCategory =
  | 'FIRE'
  | 'WATER'
  | 'BURGLARY'
  | 'VANDALISM'
  | 'NATURAL_DISASTER'
  | 'OTHER';

export interface ClaimFormData {
  policyNumber: string;
  incidentDate: string;
  damageCategory: DamageCategory;
  description: string;
  estimatedAmount: string;
  contactPhone: string;
  contactEmail: string;
  address: string;
  witnessName: string;
  policeReportNumber: string;
}

export interface PolicyInfo {
  policyNumber: string;
  holderName: string;
  address: string;
  coverageAmount: number;
  validUntil: string;
}

export interface ClaimSubmitResponse {
  claimNumber: string;
  status: 'RECEIVED';
  message: string;
}
