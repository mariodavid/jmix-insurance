import { useState } from 'react';
import type { ClaimFormData, PolicyInfo, ClaimSubmitResponse, DamageCategory } from './types';
import { lookupPolicy, submitClaim } from './api';
import './App.css';

const DAMAGE_CATEGORIES: { value: DamageCategory; label: string }[] = [
  { value: 'FIRE', label: 'Brand / Explosion' },
  { value: 'WATER', label: 'Leitungswasser / Überschwemmung' },
  { value: 'BURGLARY', label: 'Einbruchdiebstahl' },
  { value: 'VANDALISM', label: 'Vandalismus' },
  { value: 'NATURAL_DISASTER', label: 'Naturkatastrophe (Sturm, Hagel, …)' },
  { value: 'OTHER', label: 'Sonstiges' },
];

const EMPTY_FORM: ClaimFormData = {
  policyNumber: '',
  incidentDate: '',
  damageCategory: 'OTHER',
  description: '',
  estimatedAmount: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  witnessName: '',
  policeReportNumber: '',
};

type Step = 'policy-lookup' | 'claim-form' | 'success';

export default function App() {
  const [step, setStep] = useState<Step>('policy-lookup');
  const [policyInput, setPolicyInput] = useState('');
  const [policyLookupLoading, setPolicyLookupLoading] = useState(false);
  const [policyError, setPolicyError] = useState('');
  const [policy, setPolicy] = useState<PolicyInfo | null>(null);
  const [form, setForm] = useState<ClaimFormData>(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<ClaimSubmitResponse | null>(null);
  const [errors, setErrors] = useState<Partial<Record<keyof ClaimFormData, string>>>({});

  async function handlePolicyLookup(e: React.FormEvent) {
    e.preventDefault();
    setPolicyError('');
    setPolicyLookupLoading(true);
    const found = await lookupPolicy(policyInput);
    setPolicyLookupLoading(false);
    if (!found) {
      setPolicyError('Keine Police mit dieser Nummer gefunden. Bitte prüfen Sie Ihre Eingabe.');
      return;
    }
    setPolicy(found);
    setForm({ ...EMPTY_FORM, policyNumber: found.policyNumber, address: found.address });
    setStep('claim-form');
  }

  function validate(): boolean {
    const newErrors: Partial<Record<keyof ClaimFormData, string>> = {};
    if (!form.incidentDate) newErrors.incidentDate = 'Bitte Schadensdatum angeben.';
    if (!form.damageCategory) newErrors.damageCategory = 'Bitte Schadensart auswählen.';
    if (!form.description || form.description.trim().length < 20)
      newErrors.description = 'Bitte Schadensbeschreibung angeben (mind. 20 Zeichen).';
    if (!form.estimatedAmount || isNaN(Number(form.estimatedAmount)) || Number(form.estimatedAmount) <= 0)
      newErrors.estimatedAmount = 'Bitte gültigen Schadensbetrag angeben.';
    if (!form.contactPhone && !form.contactEmail)
      newErrors.contactPhone = 'Bitte mindestens Telefon oder E-Mail angeben.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setSubmitting(true);
    const response = await submitClaim(form);
    setSubmitting(false);
    setResult(response);
    setStep('success');
  }

  function field(key: keyof ClaimFormData) {
    return {
      value: form[key],
      onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
        setForm({ ...form, [key]: e.target.value }),
    };
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-inner">
          <div className="header-brand">
            <span className="brand-name">Pfefferminzia</span>
            <span className="brand-suffix">Versicherung</span>
          </div>
          <div className="header-title">
            <h1>Schadensmeldung</h1>
            <p>Hausratversicherung</p>
          </div>
        </div>
      </header>

      <main className="app-main">
        {step === 'policy-lookup' && (
          <section className="card">
            <h2>Policennummer eingeben</h2>
            <p className="hint">
              Geben Sie Ihre Policennummer ein, um die Schadensmeldung zu starten.
              <br />
              <small>Testdaten: HV-2024-001234 · HV-2023-005678 · HV-2025-009999</small>
            </p>
            <form onSubmit={handlePolicyLookup} className="lookup-form">
              <div className="form-group">
                <label htmlFor="policyNumber">Policennummer</label>
                <input
                  id="policyNumber"
                  type="text"
                  placeholder="z. B. HV-2024-001234"
                  value={policyInput}
                  onChange={(e) => setPolicyInput(e.target.value)}
                  required
                  autoFocus
                />
                {policyError && <span className="error">{policyError}</span>}
              </div>
              <button type="submit" className="btn btn-primary" disabled={policyLookupLoading}>
                {policyLookupLoading ? 'Suche läuft…' : 'Weiter'}
              </button>
            </form>
          </section>
        )}

        {step === 'claim-form' && policy && (
          <section className="card">
            <div className="policy-banner">
              <div className="policy-banner-main">
                <strong>{policy.holderName}</strong>
                <span className="policy-number">{policy.policyNumber}</span>
              </div>
              <div className="policy-details">
                <span>{policy.address}</span>
                <span>Versicherungssumme: {policy.coverageAmount.toLocaleString('de-DE')} €</span>
                <span>Gültig bis: {new Date(policy.validUntil).toLocaleDateString('de-DE')}</span>
              </div>
            </div>

            <h2>Schadensangaben</h2>
            <form onSubmit={handleSubmit} className="claim-form" noValidate>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="incidentDate">Schadensdatum *</label>
                  <input
                    id="incidentDate"
                    type="date"
                    max={new Date().toISOString().split('T')[0]}
                    {...field('incidentDate')}
                  />
                  {errors.incidentDate && <span className="error">{errors.incidentDate}</span>}
                </div>
                <div className="form-group">
                  <label htmlFor="damageCategory">Schadensart *</label>
                  <select id="damageCategory" {...field('damageCategory')}>
                    {DAMAGE_CATEGORIES.map((c) => (
                      <option key={c.value} value={c.value}>
                        {c.label}
                      </option>
                    ))}
                  </select>
                  {errors.damageCategory && <span className="error">{errors.damageCategory}</span>}
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="description">Schadensbeschreibung *</label>
                <textarea
                  id="description"
                  rows={4}
                  placeholder="Beschreiben Sie bitte den Schaden ausführlich…"
                  {...field('description')}
                />
                <div className="char-counter">
                  <span className={form.description.trim().length >= 20 ? 'char-ok' : 'char-pending'}>
                    {form.description.trim().length} / 20 Zeichen Mindestlänge
                  </span>
                </div>
                {errors.description && <span className="error">{errors.description}</span>}
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="estimatedAmount">Geschätzter Schaden (€) *</label>
                  <input
                    id="estimatedAmount"
                    type="number"
                    min="1"
                    step="0.01"
                    placeholder="0,00"
                    {...field('estimatedAmount')}
                  />
                  {errors.estimatedAmount && <span className="error">{errors.estimatedAmount}</span>}
                </div>
                <div className="form-group">
                  <label htmlFor="address">Schadensort</label>
                  <input id="address" type="text" {...field('address')} />
                </div>
              </div>

              <h3>Kontaktdaten</h3>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="contactPhone">Telefon</label>
                  <input id="contactPhone" type="tel" placeholder="+49 89 12345678" {...field('contactPhone')} />
                  {errors.contactPhone && <span className="error">{errors.contactPhone}</span>}
                </div>
                <div className="form-group">
                  <label htmlFor="contactEmail">E-Mail</label>
                  <input id="contactEmail" type="email" placeholder="name@beispiel.de" {...field('contactEmail')} />
                </div>
              </div>

              <h3>Weitere Angaben (optional)</h3>
              <div className="form-row">
                <div className="form-group">
                  <label htmlFor="witnessName">Zeuge (Name)</label>
                  <input id="witnessName" type="text" {...field('witnessName')} />
                </div>
                <div className="form-group">
                  <label htmlFor="policeReportNumber">Polizeiliches Aktenzeichen</label>
                  <input id="policeReportNumber" type="text" {...field('policeReportNumber')} />
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => { setStep('policy-lookup'); setErrors({}); }}
                >
                  Zurück
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Wird übermittelt…' : 'Schaden melden'}
                </button>
              </div>
            </form>
          </section>
        )}

        {step === 'success' && result && (
          <section className="card success-card">
            <div className="success-icon">✓</div>
            <h2>Schadensmeldung eingegangen</h2>
            <p>{result.message}</p>
            <div className="claim-number-box">
              <span>Ihr Aktenzeichen</span>
              <strong>{result.claimNumber}</strong>
            </div>
            <p className="hint">
              Sie erhalten in Kürze eine Bestätigung per E-Mail. Ein Schadenssachbearbeiter
              wird sich innerhalb von 2 Werktagen bei Ihnen melden.
            </p>
            <button
              className="btn btn-primary"
              onClick={() => {
                setStep('policy-lookup');
                setPolicyInput('');
                setPolicy(null);
                setForm(EMPTY_FORM);
                setResult(null);
              }}
            >
              Neue Schadensmeldung
            </button>
          </section>
        )}
      </main>
    </div>
  );
}
