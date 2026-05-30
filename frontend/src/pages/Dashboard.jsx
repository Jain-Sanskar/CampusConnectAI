import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import ResourceCard from '../components/ResourceCard'
import { listResources } from '../api/resourceApi'
import { useAuth } from '../context/AuthContext'

export default function Dashboard() {
  const { user } = useAuth()

  const [resources, setResources] = useState([])
  const [categories, setCategories] = useState([])
  const [filters, setFilters] = useState({ category: '', subject: '' })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchResources = async (activeFilters) => {
    setLoading(true)
    setError('')
    try {
      const data = await listResources(activeFilters)
      setResources(data)
    } catch {
      setError('Could not load resources. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  // initial load doubles as the source for the category dropdown
  useEffect(() => {
    listResources()
      .then((data) => {
        setResources(data)
        setCategories([...new Set(data.map((r) => r.category).filter(Boolean))])
      })
      .catch(() => setError('Could not load resources. Please try again.'))
      .finally(() => setLoading(false))
  }, [])

  const handleFilterChange = (event) => {
    const next = { ...filters, [event.target.name]: event.target.value }
    setFilters(next)
    fetchResources(next)
  }

  const clearFilters = () => {
    const cleared = { category: '', subject: '' }
    setFilters(cleared)
    fetchResources(cleared)
  }

  const hasFilters = filters.category || filters.subject

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="max-w-6xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-slate-800">
          Hi {user?.name || 'there'} 👋
        </h1>
        <p className="mt-1 text-slate-500">Browse academic resources shared for you.</p>

        <div className="mt-6 flex flex-col sm:flex-row gap-3 sm:items-end">
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Category</label>
            <select
              name="category"
              value={filters.category}
              onChange={handleFilterChange}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
            >
              <option value="">All categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Subject</label>
            <input
              name="subject"
              value={filters.subject}
              onChange={handleFilterChange}
              placeholder="e.g. Operating Systems"
              className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-brand focus:ring-1 focus:ring-brand outline-none"
            />
          </div>

          {hasFilters && (
            <button
              onClick={clearFilters}
              className="text-sm font-medium text-slate-500 hover:text-brand px-2 py-2"
            >
              Clear filters
            </button>
          )}
        </div>

        {error && (
          <div className="mt-6 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="mt-6">
          {loading ? (
            <p className="text-slate-400">Loading resources…</p>
          ) : resources.length === 0 ? (
            <div className="text-center py-16 text-slate-400">
              No resources found{hasFilters ? ' for these filters.' : ' yet.'}
            </div>
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {resources.map((resource) => (
                <ResourceCard key={resource.id} resource={resource} />
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
