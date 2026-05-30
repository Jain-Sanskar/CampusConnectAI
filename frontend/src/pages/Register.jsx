import { Link } from 'react-router-dom'

export default function Register() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md bg-white rounded-xl shadow-sm border border-slate-200 p-8 text-center">
        <h1 className="text-2xl font-bold text-slate-800">Create your account</h1>
        <p className="mt-2 text-sm text-slate-500">Registration screen coming up next.</p>
        <p className="mt-4 text-sm text-slate-500">
          Already have an account?{' '}
          <Link to="/login" className="text-brand font-medium">
            Login
          </Link>
        </p>
      </div>
    </div>
  )
}
