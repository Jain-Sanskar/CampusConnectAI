import Navbar from '../components/Navbar'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { user } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="max-w-6xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-slate-800">
          Hi {user?.name || 'there'} 👋
        </h1>
        <p className="mt-2 text-slate-500">
          Your academic resources and AI mentor will appear here.
        </p>
      </main>
    </div>
  )
}
