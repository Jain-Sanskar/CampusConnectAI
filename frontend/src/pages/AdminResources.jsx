import { useEffect, useState } from 'react'
import Navbar from '../components/Navbar'
import ResourceFormModal from '../components/ResourceFormModal'
import Spinner from '../components/Spinner'
import {
  listResources,
  createResource,
  updateResource,
  deleteResource,
} from '../api/resourceApi'

export default function AdminResources() {
  const [resources, setResources] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')

  const loadResources = () => {
    setLoading(true)
    setError('')
    listResources()
      .then(setResources)
      .catch(() => setError('Could not load resources.'))
      .finally(() => setLoading(false))
  }

  useEffect(loadResources, [])

  const openCreate = () => {
    setEditing(null)
    setFormError('')
    setModalOpen(true)
  }

  const openEdit = (resource) => {
    setEditing(resource)
    setFormError('')
    setModalOpen(true)
  }

  const handleSave = async (payload) => {
    setSaving(true)
    setFormError('')
    try {
      if (editing) {
        await updateResource(editing.id, payload)
      } else {
        await createResource(payload)
      }
      setModalOpen(false)
      loadResources()
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not save the resource.')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (resource) => {
    if (!window.confirm(`Delete "${resource.title}"?`)) return
    try {
      await deleteResource(resource.id)
      setResources((prev) => prev.filter((r) => r.id !== resource.id))
    } catch {
      setError('Could not delete the resource.')
    }
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-800">Manage resources</h1>
            <p className="mt-1 text-slate-500">Add, edit or remove academic resources.</p>
          </div>
          <button
            onClick={openCreate}
            className="rounded-md bg-brand px-4 py-2 text-sm font-semibold text-white hover:bg-brand-dark transition"
          >
            + Add resource
          </button>
        </div>

        {error && (
          <div className="mt-6 rounded-md bg-red-50 border border-red-200 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="mt-6 bg-white rounded-lg border border-slate-200 overflow-hidden">
          {loading ? (
            <p className="p-6 text-slate-400 flex items-center gap-2">
              <Spinner /> Loading…
            </p>
          ) : resources.length === 0 ? (
            <p className="p-6 text-slate-400">No resources yet. Add your first one.</p>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left text-slate-500">
                <tr>
                  <th className="px-4 py-3 font-medium">Title</th>
                  <th className="px-4 py-3 font-medium hidden sm:table-cell">Category</th>
                  <th className="px-4 py-3 font-medium hidden md:table-cell">Subject</th>
                  <th className="px-4 py-3 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {resources.map((resource) => (
                  <tr key={resource.id}>
                    <td className="px-4 py-3 text-slate-800">{resource.title}</td>
                    <td className="px-4 py-3 text-slate-500 hidden sm:table-cell">
                      {resource.category}
                    </td>
                    <td className="px-4 py-3 text-slate-500 hidden md:table-cell">
                      {resource.subject}
                    </td>
                    <td className="px-4 py-3 text-right space-x-3 whitespace-nowrap">
                      <button
                        onClick={() => openEdit(resource)}
                        className="font-medium text-brand hover:underline"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDelete(resource)}
                        className="font-medium text-red-600 hover:underline"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </main>

      {modalOpen && (
        <ResourceFormModal
          initial={editing}
          saving={saving}
          serverError={formError}
          onClose={() => setModalOpen(false)}
          onSave={handleSave}
        />
      )}
    </div>
  )
}
