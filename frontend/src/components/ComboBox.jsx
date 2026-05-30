import { useState } from 'react'

const ADD_NEW = '__add_new__'

/**
 * A dropdown of existing values with an inline "+ Add new…" option.
 * Picking "Add new" switches to a free-text input; whatever is typed becomes the
 * value and is persisted (and so appears in the dropdown) once the resource is saved.
 *
 * onChange is called as onChange(name, value) to plug into the parent form state.
 */
export default function ComboBox({
  label,
  name,
  value,
  options = [],
  onChange,
  error,
  placeholder = '',
  required = false,
}) {
  const [adding, setAdding] = useState(false)

  // a value not present in the options list means we're entering a custom one
  const isCustom = adding || (Boolean(value) && !options.includes(value))

  const handleSelect = (event) => {
    const selected = event.target.value
    if (selected === ADD_NEW) {
      setAdding(true)
      onChange(name, '')
    } else {
      setAdding(false)
      onChange(name, selected)
    }
  }

  const inputClass =
    'mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none'

  return (
    <div>
      <label htmlFor={name} className="block text-sm font-medium text-slate-700">
        {label}
        {required && <span className="text-red-500"> *</span>}
      </label>

      {isCustom ? (
        <div className="mt-1 flex gap-2">
          <input
            id={name}
            name={name}
            type="text"
            value={value}
            onChange={(e) => onChange(name, e.target.value)}
            placeholder={placeholder || 'Type a new value'}
            className={inputClass.replace('mt-1 ', '')}
            autoFocus
          />
          {options.length > 0 && (
            <button
              type="button"
              onClick={() => {
                setAdding(false)
                onChange(name, '')
              }}
              className="shrink-0 text-xs font-medium text-slate-500 hover:text-brand whitespace-nowrap"
            >
              Pick from list
            </button>
          )}
        </div>
      ) : (
        <select
          id={name}
          name={name}
          value={options.includes(value) ? value : ''}
          onChange={handleSelect}
          className={inputClass}
        >
          <option value="">Select…</option>
          {options.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
          <option value={ADD_NEW}>+ Add new…</option>
        </select>
      )}

      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  )
}
