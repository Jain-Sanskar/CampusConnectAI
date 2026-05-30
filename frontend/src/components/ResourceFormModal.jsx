import { useState } from 'react'

const EMPTY = {
  title: '',
  description: '',
  category: '',
  subject: '',
  resourceUrl: '',
  type: '',
}

const isValidUrl = (value) => {
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

export default function ResourceFormModal({ initial, saving, serverError, onClose, onSave }) {
  const [form, setForm] = useState(initial ? { ...EMPTY, ...initial } : EMPTY)
  const [errors, setErrors] = useState({})

  const validate = () => {
    const next = {}
    if (!form.title.trim()) next.title = 'Title is required'
    if (!form.category.trim()) next.category = 'Category is required'
    if (!form.subject.trim()) next.subject = 'Subject is required'
    if (!form.resourceUrl.trim()) {
      next.resourceUrl = 'Resource URL is required'
    } else if (!isValidUrl(form.resourceUrl)) {
      next.resourceUrl = 'Enter a valid http(s) URL'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    if (validate()) onSave(form)
  }

  const field = (name, label, { type = 'text', textarea = false, placeholder = '' } = {}) => (
    <div>
      <label htmlFor={name} className="block text-sm font-medium text-slate-700">
        {label}
      </label>
      {textarea ? (
        <textarea
          id={name}
          name={name}
          rows={3}
          value={form[name]}
          onChange={handleChange}
          placeholder={placeholder}
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
        />
      ) : (
        <input
          id={name}
          name={name}
          type={type}
          value={form[name]}
          onChange={handleChange}
          placeholder={placeholder}
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
        />
      )}
      {errors[name] && <p className="mt-1 text-xs text-red-600">{errors[name]}</p>}
    </div>
  )

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 px-4">
      <div className="w-full max-w-lg bg-white rounded-xl shadow-lg p-6 max-h-[90vh] overflow-y-auto">
        <h2 className="text-lg font-bold text-slate-800">
          {initial ? 'Edit resource' : 'Add resource'}
        </h2>

        {serverError && (
          <div className="mt-3 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-4 space-y-4" noValidate>
          {field('title', 'Title', { placeholder: 'DBMS Complete Notes' })}
          {field('description', 'Description', {
            textarea: true,
            placeholder: 'Short summary of what this covers',
          })}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {field('category', 'Category', { placeholder: 'Notes / PYQ / Video' })}
            {field('subject', 'Subject', { placeholder: 'Operating Systems' })}
          </div>
          {field('resourceUrl', 'Resource URL', { placeholder: 'https://…' })}
          {field('type', 'Type (optional)', { placeholder: 'PDF / Link / Video' })}

          <div className="flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="text-sm font-medium text-slate-500 hover:text-slate-700 px-4 py-2"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-dark disabled:opacity-60 transition"
            >
              {saving ? 'Saving…' : 'Save'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
