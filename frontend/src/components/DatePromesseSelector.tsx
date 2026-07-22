import { useState } from 'react';
import DatePicker, { registerLocale } from 'react-datepicker';
import { fr } from 'date-fns/locale/fr';
import 'react-datepicker/dist/react-datepicker.css';
import './DatePromesseSelector.css';

// Enregistrer la locale française
registerLocale('fr', fr);

interface DatePromesseSelectorProps {
  value: string;
  onChange: (dateString: string) => void;
  disabled?: boolean;
  required?: boolean;
}

export default function DatePromesseSelector({
  value,
  onChange,
  disabled = false,
  required = false,
}: DatePromesseSelectorProps) {
  const [isOpen, setIsOpen] = useState(false);

  const parseDate = (dateString: string): Date | null => {
    if (!dateString) return null;
    const date = new Date(dateString);
    return isNaN(date.getTime()) ? null : date;
  };

  const handleDateChange = (date: Date | null) => {
    if (date) {
      const dateString = date.toISOString().split('T')[0];
      onChange(dateString);
      setIsOpen(false);
    }
  };

  const selectedDate = parseDate(value);
  const minDate = new Date();
  minDate.setHours(0, 0, 0, 0);

  return (
    <div className="date-promesse-selector">
      <DatePicker
        selected={selectedDate}
        onChange={handleDateChange}
        dateFormat="dd/MM/yyyy"
        minDate={minDate}
        placeholderText="JJ/MM/AAAA"
        disabled={disabled}
        required={required}
        open={isOpen}
        onCalendarOpen={() => setIsOpen(true)}
        onCalendarClose={() => setIsOpen(false)}
        popperClassName="date-promesse-popper"
        className="date-promesse-input"
        showYearDropdown
        showMonthDropdown
        dropdownMode="select"
        locale="fr"
        calendarStartDay={1}
        showPopperArrow={false}
        fixedHeight
      />
      <span className="calendar-icon" onClick={() => !disabled && setIsOpen(!isOpen)}>
        📅
      </span>
    </div>
  );
}
