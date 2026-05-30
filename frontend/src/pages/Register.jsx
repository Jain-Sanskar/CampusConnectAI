import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import axiosClient from '../api/axiosClient'
import Spinner from '../components/Spinner'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { login } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [serverError, setServerError] = useState('')
  const [loading, setLoading] = useState(false)

  const validate = () => {
    const next = {}
    if (!form.name.trim()) {
      next.name = 'Name is required'
    } else if (form.name.trim().length < 2) {
      next.name = 'Name must be at least 2 characters'
    }
    if (!form.email.trim()) {
      next.email = 'Email is required'
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      next.email = 'Enter a valid email address'
    }
    if (!form.password) {
      next.password = 'Password is required'
    } else if (form.password.length < 6) {
      next.password = 'Password must be at least 6 characters'
    }
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setServerError('')
    if (!validate()) return

    setLoading(true)
    try {
      const { data } = await axiosClient.post('/auth/register', form)
      login(data)
      navigate('/')
    } catch (err) {
      setServerError(
        err.response?.data?.message || 'Registration failed. Please try again.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md bg-white rounded-xl shadow-sm border border-slate-200 p-8">
        <h1 className="text-2xl font-bold text-slate-800 text-center">Create your account</h1>
        <p className="mt-1 text-sm text-slate-500 text-center">
          Join CampusConnect AI to get started.
        </p>

        {serverError && (
          <div className="mt-5 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-6 space-y-4" noValidate>
          <div>
            <label htmlFor="name" className="block text-sm font-medium text-slate-700">
              Full name
            </label>
            <input
              id="name"
              name="name"
              type="text"
              value={form.name}
              onChange={handleChange}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
              placeholder="Sanskar Jain"
            />
            {errors.name && <p className="mt-1 text-xs text-red-600">{errors.name}</p>}
          </div>

          <div>
            <label htmlFor="email" className="block text-sm font-medium text-slate-700">
              Email
            </label>
            <input
              id="email"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
              placeholder="you@college.edu"
            />
            {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email}</p>}
          </div>

          <div>
            <label htmlFor="password" className="block text-sm font-medium text-slate-700">
              Password
            </label>
            <input
              id="password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
              placeholder="At least 6 characters"
            />
            {errors.password && <p className="mt-1 text-xs text-red-600">{errors.password}</p>}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-md bg-brand py-2 text-sm font-semibold text-white hover:bg-brand-dark disabled:opacity-60 transition"
          >
            {loading ? (
              <span className="inline-flex items-center justify-center gap-2">
                <Spinner /> Creating account…
              </span>
            ) : (
              'Sign up'
            )}
          </button>
        </form>

        <p className="mt-5 text-sm text-slate-500 text-center">
          Already have an account?{' '}
          <Link to="/login" className="text-brand font-medium hover:underline">
            Login
          </Link>
        </p>
      </div>
    </div>
  )
}
