import { Link } from 'react-router-dom'

export default function Login() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md bg-white rounded-xl shadow-sm border border-slate-200 p-8 text-center">
        <h1 className="text-2xl font-bold text-slate-800">Welcome back</h1>
        <p className="mt-2 text-sm text-slate-500">Login screen coming up next.</p>
        <p className="mt-4 text-sm text-slate-500">
          New here?{' '}
          <Link to="/register" className="text-brand font-medium">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  )
}
