import { useState, useRef } from 'react'
import './App.css'

// ── Constants ────────────────────────────────────────────────────
const DAMAGE_TYPES = [
  'Einbruch / Diebstahl',
  'Brand / Feuer',
  'Leitungswasser',
  'Sturm / Hagel',
  'Vandalismus',
  'Überschwemmung',
  'Sonstiges',
]

const STEPS = [
  { label: 'Police'    },
  { label: 'Kontakt'   },
  { label: 'Schaden'   },
  { label: 'Dokumente' },
  { label: 'Prüfen'    },
]

const INITIAL = {
  // step 1
  policyNumber: '',
  // step 2
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  // step 3
  damageDate: '',
  damageTime: '',
  damageType: '',
  damageLocation: '',
  description: '',
  estimatedDamage: '',
  // step 4
  files: [],
  // step 5
  consent: false,
}

function genRef() {
  return 'SCH-' + String(Math.floor(100000 + Math.random() * 900000))
}

// ── Small components ─────────────────────────────────────────────
function Header() {
  return (
    <header className="header">
      <div className="header-logo">P</div>
      <div className="header-brand">
        <span className="header-brand-name">Pfefferminzia</span>
        <span className="header-brand-tagline">Versicherungen</span>
      </div>
    </header>
  )
}

function Footer() {
  return (
    <footer className="footer">
      © 2026 Pfefferminzia Versicherungen AG · Alle Rechte vorbehalten
    </footer>
  )
}

function Field({ label, required, hint, error, children }) {
  return (
    <div className="field">
      <label>{label}{required && <span className="req"> *</span>}</label>
      {children}
      {hint  && !error && <span className="field-hint">{hint}</span>}
      {error && <span className="field-error">{error}</span>}
    </div>
  )
}

// ── Stepper ───────────────────────────────────────────────────────
function Stepper({ step }) {
  const total = STEPS.length
  const pct   = step === 0 ? 0 : (step / (total - 1)) * 100

  return (
    <div className="stepper">
      <div className="stepper-progress" style={{ width: `${pct}%` }} />
      {STEPS.map((s, i) => {
        const state = i < step ? 'done' : i === step ? 'active' : ''
        return (
          <div key={i} className={`step-item ${state}`}>
            <div className="step-bubble">
              {i < step ? '✓' : i + 1}
            </div>
            <span className="step-label">{s.label}</span>
          </div>
        )
      })}
    </div>
  )
}

// ── Steps ─────────────────────────────────────────────────────────
function Step1({ form, set, errors }) {
  return (
    <>
      <div className="card-head">
        <h2>Schritt 1 – Policendaten</h2>
        <p>Geben Sie die Policennummer Ihrer Hausratversicherung ein.</p>
      </div>
      <div className="card-body">
        <div className="form-row full">
          <Field label="Policennummer" required error={errors.policyNumber}>
            <input
              type="text"
              placeholder="z. B. HR-2024-001234"
              value={form.policyNumber}
              className={errors.policyNumber ? 'err' : ''}
              onChange={e => set('policyNumber', e.target.value)}
            />
          </Field>
        </div>
      </div>
    </>
  )
}

function Step2({ form, set, errors }) {
  return (
    <>
      <div className="card-head">
        <h2>Schritt 2 – Kontaktdaten</h2>
        <p>Wie können wir Sie im Schadensfall erreichen?</p>
      </div>
      <div className="card-body">
        <div className="form-row" style={{ marginBottom: 16 }}>
          <Field label="Vorname" required error={errors.firstName}>
            <input
              type="text"
              placeholder="Max"
              value={form.firstName}
              className={errors.firstName ? 'err' : ''}
              onChange={e => set('firstName', e.target.value)}
            />
          </Field>
          <Field label="Nachname" required error={errors.lastName}>
            <input
              type="text"
              placeholder="Mustermann"
              value={form.lastName}
              className={errors.lastName ? 'err' : ''}
              onChange={e => set('lastName', e.target.value)}
            />
          </Field>
        </div>
        {/* Email OR phone — at least one required */}
        <div className="form-row">
          <Field label="E-Mail-Adresse" error={errors.email}>
            <input
              type="email"
              placeholder="max@beispiel.de"
              value={form.email}
              className={errors.email || errors.contact ? 'err' : ''}
              onChange={e => set('email', e.target.value)}
            />
          </Field>
          <Field label="Telefonnummer">
            <input
              type="tel"
              placeholder="+49 89 12345678"
              value={form.phone}
              className={errors.contact ? 'err' : ''}
              onChange={e => set('phone', e.target.value)}
            />
          </Field>
        </div>
        {errors.contact && (
          <span className="field-error" style={{ marginTop: 6, display: 'block' }}>
            {errors.contact}
          </span>
        )}
      </div>
    </>
  )
}

function Step3({ form, set, errors }) {
  const today = new Date().toISOString().split('T')[0]
  return (
    <>
      <div className="card-head">
        <h2>Schritt 3 – Schadendetails</h2>
        <p>Beschreiben Sie den entstandenen Schaden.</p>
      </div>
      <div className="card-body">
        <div className="form-row" style={{ marginBottom: 16 }}>
          <Field label="Schadendatum" required error={errors.damageDate}>
            <input
              type="date"
              max={today}
              value={form.damageDate}
              className={errors.damageDate ? 'err' : ''}
              onChange={e => set('damageDate', e.target.value)}
            />
          </Field>
          <Field label="Uhrzeit (ca.)">
            <input
              type="time"
              value={form.damageTime}
              onChange={e => set('damageTime', e.target.value)}
            />
          </Field>
        </div>
        <div className="form-row full" style={{ marginBottom: 16 }}>
          <Field label="Schadensart" required error={errors.damageType}>
            <select
              value={form.damageType}
              className={errors.damageType ? 'err' : ''}
              onChange={e => set('damageType', e.target.value)}
            >
              <option value="">— bitte auswählen —</option>
              {DAMAGE_TYPES.map(t => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </Field>
        </div>
        <div className="form-row full" style={{ marginBottom: 16 }}>
          <Field label="Schadensort / betroffene Adresse">
            <input
              type="text"
              placeholder="Musterstraße 1, 80331 München"
              value={form.damageLocation}
              onChange={e => set('damageLocation', e.target.value)}
            />
          </Field>
        </div>
        <div className="form-row full" style={{ marginBottom: 16 }}>
          <Field label="Schadenbeschreibung" required error={errors.description}>
            <textarea
              placeholder="Was ist genau passiert? Welche Gegenstände wurden beschädigt oder entwendet? Wie wurden die Gegenstände beschädigt?"
              value={form.description}
              className={errors.description ? 'err' : ''}
              onChange={e => set('description', e.target.value)}
              rows={5}
            />
          </Field>
        </div>
        <div className="form-row full">
          <Field
            label="Geschätzter Schadenbetrag (€)"
            hint="Ungefähre Angabe genügt"
          >
            <input
              type="number"
              min="0"
              step="1"
              placeholder="0"
              value={form.estimatedDamage}
              onChange={e => set('estimatedDamage', e.target.value)}
            />
          </Field>
        </div>
      </div>
    </>
  )
}

function Step4({ form, set }) {
  const inputRef = useRef(null)

  function handleFiles(fileList) {
    const added = Array.from(fileList).map(f => ({ name: f.name, size: f.size }))
    set('files', [...form.files, ...added])
  }

  function removeFile(idx) {
    set('files', form.files.filter((_, i) => i !== idx))
  }

  return (
    <>
      <div className="card-head">
        <h2>Schritt 4 – Dokumente &amp; Fotos</h2>
        <p>Fotos, Rechnungen und Kostenvoranschläge helfen uns bei der Bearbeitung.</p>
      </div>
      <div className="card-body">
        <div className="file-drop">
          <input
            ref={inputRef}
            type="file"
            multiple
            accept="image/*,.pdf"
            onChange={e => handleFiles(e.target.files)}
          />
          <div className="file-drop-icon">📎</div>
          <p className="file-drop-hint">
            <strong>Dateien auswählen</strong> oder hierher ziehen<br />
            <span style={{ fontSize: 12 }}>JPG, PNG, PDF · max. 10 MB je Datei</span>
          </p>
        </div>
        {form.files.length > 0 && (
          <div className="file-list">
            {form.files.map((f, i) => (
              <div key={i} className="file-item">
                <span>📄</span>
                <span>{f.name}</span>
                <span className="file-meta">({(f.size / 1024).toFixed(0)} KB)</span>
                <button
                  type="button"
                  className="file-remove"
                  onClick={() => removeFile(i)}
                  aria-label="Datei entfernen"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        )}
        <p className="field-hint" style={{ marginTop: 12 }}>
          Dieser Schritt ist optional. Sie können Unterlagen auch später nachreichen.
        </p>
      </div>
    </>
  )
}

function SummaryRow({ label, value }) {
  if (!value && value !== 0) return null
  return (
    <div className="summary-row">
      <span className="summary-key">{label}</span>
      <span className="summary-value">{value}</span>
    </div>
  )
}

function Step5({ form, set, errors }) {
  return (
    <>
      <div className="card-head">
        <h2>Schritt 5 – Zusammenfassung &amp; Absenden</h2>
        <p>Bitte prüfen Sie Ihre Angaben vor dem Absenden.</p>
      </div>
      <div className="card-body">
        {/* Police */}
        <div className="summary-group">
          <div className="summary-group-title">Policendaten</div>
          <SummaryRow label="Policennummer" value={form.policyNumber} />
        </div>
        {/* Kontakt */}
        <div className="summary-group">
          <div className="summary-group-title">Kontaktdaten</div>
          <SummaryRow label="Name"      value={`${form.firstName} ${form.lastName}`.trim()} />
          <SummaryRow label="E-Mail"    value={form.email}    />
          <SummaryRow label="Telefon"   value={form.phone}    />
        </div>
        {/* Schaden */}
        <div className="summary-group">
          <div className="summary-group-title">Schadendetails</div>
          <SummaryRow label="Datum"       value={form.damageDate}      />
          <SummaryRow label="Uhrzeit"     value={form.damageTime || '—'} />
          <SummaryRow label="Schadensart" value={form.damageType}      />
          <SummaryRow label="Ort"         value={form.damageLocation}  />
          <SummaryRow label="Geschätzter Betrag"
            value={form.estimatedDamage ? `${Number(form.estimatedDamage).toLocaleString('de-DE')} €` : undefined}
          />
          <div className="summary-row" style={{ flexDirection: 'column', alignItems: 'flex-start', gap: 4 }}>
            <span className="summary-key">Beschreibung</span>
            <span className="summary-value" style={{ textAlign: 'left', fontWeight: 400, color: 'var(--gray-700)', fontSize: 13, whiteSpace: 'pre-wrap' }}>
              {form.description}
            </span>
          </div>
        </div>
        {/* Dokumente */}
        {form.files.length > 0 && (
          <div className="summary-group">
            <div className="summary-group-title">Dokumente</div>
            <SummaryRow label="Dateien" value={`${form.files.length} Datei(en) angehängt`} />
          </div>
        )}

        {/* Consent */}
        <div className="consent" style={{ marginTop: 24 }}>
          <input
            type="checkbox"
            id="consent"
            checked={form.consent}
            onChange={e => set('consent', e.target.checked)}
          />
          <p>
            Ich stimme der Verarbeitung meiner personenbezogenen Daten gemäß der{' '}
            <a href="#" style={{ color: 'var(--teal-700)' }}>Datenschutzerklärung</a>{' '}
            der Pfefferminzia Versicherungen AG zu.
          </p>
        </div>
        {errors.consent && (
          <span className="field-error" style={{ display: 'block', marginTop: 6 }}>
            {errors.consent}
          </span>
        )}
      </div>
    </>
  )
}

// ── Validation per step ───────────────────────────────────────────
function validateStep(step, form) {
  const e = {}
  if (step === 0) {
    if (!form.policyNumber.trim()) e.policyNumber = 'Policennummer erforderlich.'
  }
  if (step === 1) {
    if (!form.firstName.trim()) e.firstName = 'Vorname erforderlich.'
    if (!form.lastName.trim())  e.lastName  = 'Nachname erforderlich.'
    if (!form.email.trim() && !form.phone.trim()) {
      e.contact = 'Bitte E-Mail-Adresse oder Telefonnummer angeben.'
    } else if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      e.email = 'Ungültige E-Mail-Adresse.'
    }
  }
  if (step === 2) {
    if (!form.damageDate)         e.damageDate   = 'Schadendatum erforderlich.'
    if (!form.damageType)         e.damageType   = 'Schadensart erforderlich.'
    if (!form.description.trim()) e.description  = 'Beschreibung erforderlich.'
  }
  if (step === 4) {
    if (!form.consent) e.consent = 'Bitte stimmen Sie der Datenschutzerklärung zu.'
  }
  return e
}

// ── App ───────────────────────────────────────────────────────────
export default function App() {
  const [step, setStep]         = useState(0)
  const [form, setForm]         = useState(INITIAL)
  const [errors, setErrors]     = useState({})
  const [submitted, setSubmitted] = useState(false)
  const [refNumber, setRefNumber] = useState('')

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }))
    setErrors(e => { const n = { ...e }; delete n[field]; return n })
  }

  function goNext() {
    const errs = validateStep(step, form)
    if (Object.keys(errs).length) {
      setErrors(errs)
      return
    }
    setErrors({})
    setStep(s => s + 1)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function goBack() {
    setErrors({})
    setStep(s => s - 1)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function handleSubmit() {
    const errs = validateStep(4, form)
    if (Object.keys(errs).length) {
      setErrors(errs)
      return
    }
    setRefNumber(genRef())
    setSubmitted(true)
  }

  function handleReset() {
    setForm(INITIAL)
    setErrors({})
    setStep(0)
    setSubmitted(false)
  }

  if (submitted) {
    return (
      <div className="page">
        <Header />
        <main className="main">
          <div className="success-card">
            <div className="success-check">✓</div>
            <h2 className="success-title">Schaden erfolgreich gemeldet</h2>
            <p className="success-text">
              Vielen Dank! Ihre Meldung ist eingegangen. Wir melden uns
              innerhalb von 2 Werktagen bei Ihnen.
            </p>
            <p className="success-text" style={{ marginTop: 8 }}>Ihr Aktenzeichen:</p>
            <span className="success-ref">{refNumber}</span>
            <div className="success-new">
              <button className="btn btn-primary" onClick={handleReset}>
                Weiteren Schaden melden
              </button>
            </div>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  return (
    <div className="page">
      <Header />
      <main className="main">
        <div className="wizard">
          <Stepper step={step} />
          <div className="card">
            {step === 0 && <Step1 form={form} set={set} errors={errors} />}
            {step === 1 && <Step2 form={form} set={set} errors={errors} />}
            {step === 2 && <Step3 form={form} set={set} errors={errors} />}
            {step === 3 && <Step4 form={form} set={set} />}
            {step === 4 && <Step5 form={form} set={set} errors={errors} />}

            <div className="nav" style={{ margin: '0 32px 32px' }}>
              {step > 0
                ? <button className="btn btn-ghost" onClick={goBack}>← Zurück</button>
                : <span />
              }
              {step < 4
                ? <button className="btn btn-primary" onClick={goNext}>Weiter →</button>
                : (
                  <button className="btn btn-submit btn-primary" onClick={handleSubmit}>
                    Schaden melden ✓
                  </button>
                )
              }
            </div>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}
