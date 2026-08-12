import { useState } from 'react';
import DatePicker, { registerLocale } from 'react-datepicker';
import { fr } from 'date-fns/locale/fr';
import 'react-datepicker/dist/react-datepicker.css';
import './DatePromesseSelector.css';

registerLocale('fr', fr);

interface DatePromesseSelectorProps {
  value: string;       // "YYYY-MM-DD"
  onChange: (dateString: string) => void;
  disabled?: boolean;
  required?: boolean;
}

/**
 * Convertit "YYYY-MM-DD" → Date locale sans décalage timezone.
 * new Date("2025-07-29") = UTC 00:00 → en UTC+1 affiche 28 juillet.
 * On passe par les parts pour forcer la date locale.
 */
function parseDateLocal(dateString: string): Date | null {
  if (!dateString) return null;
  const [y, m, d] = dateString.split('-').map(Number);
  if (!y || !m || !d) return null;
  const date = new Date(y, m - 1, d);   // Date locale — aucun UTC
  return isNaN(date.getTime()) ? null : date;
}

/**
 * Convertit un objet Date local → "YYYY-MM-DD" sans passer par toISOString()
 * (toISOString convertit en UTC et peut décaler d'un jour).
 */
function formatDateLocal(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

/* ── SVG Calendar Icon ─────────────────────────────────── */
const IconCalendar = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
    <line x1="16" y1="2" x2="16" y2="6"/>
    <line x1="8"  y1="2" x2="8"  y2="6"/>
    <line x1="3"  y1="10" x2="21" y2="10"/>
  </svg>
);

const IconClear = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
    <line x1="18" y1="6" x2="6" y2="18"/>
    <line x1="6"  y1="6" x2="18" y2="18"/>
  </svg>
);

export default function DatePromesseSelector({
  value,
  onChange,
  disabled = false,
  required = false,
}: DatePromesseSelectorProps) {
  const [isOpen, setIsOpen] = useState(false);

  const handleDateChange = (date: Date | null) => {
    if (date) {
      onChange(formatDateLocal(date));   // ✅ pas d'UTC shift
    }
    setIsOpen(false);
  };

  const handleClear = (e: React.MouseEvent) => {
    e.stopPropagation();
    onChange('');
  };

  const selectedDate = parseDateLocal(value);

  // minDate = aujourd'hui local, sans heures
  const minDate = new Date();
  minDate.setHours(0, 0, 0, 0);

  return (
    <div className="dps-wrapper">
      <div
        className={`dps-input-row${isOpen ? ' dps-open' : ''}${disabled ? ' dps-disabled' : ''}`}
        onClick={() => !disabled && setIsOpen(o => !o)}
      >
        <span className="dps-icon-left"><IconCalendar /></span>
        <span className={`dps-value${!value ? ' dps-placeholder' : ''}`}>
          {selectedDate
            ? selectedDate.toLocaleDateString('fr-MA', { day: '2-digit', month: 'long', year: 'numeric' })
            : 'Sélectionner une date'}
        </span>
        {value && !disabled && (
          <button className="dps-clear" onClick={handleClear} title="Effacer">
            <IconClear />
          </button>
        )}
        <span className="dps-chevron" style={{ transform: isOpen ? 'rotate(180deg)' : 'none' }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
        </span>
      </div>

      {/* DatePicker caché — utilisé uniquement comme moteur calendrier */}
      <DatePicker
        selected={selectedDate}
        onChange={handleDateChange}
        dateFormat="dd/MM/yyyy"
        minDate={minDate}
        disabled={disabled}
        required={required}
        open={isOpen}
        onCalendarOpen={() => setIsOpen(true)}
        onCalendarClose={() => setIsOpen(false)}
        popperClassName="dps-popper"
        className="dps-hidden-input"
        showYearDropdown
        showMonthDropdown
        dropdownMode="select"
        locale="fr"
        calendarStartDay={1}
        showPopperArrow={false}
        fixedHeight
        // On cache l'input natif, on utilise notre propre trigger
        customInput={<span />}
      />
    </div>
  );
}
