import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const linkClass = ({ isActive }) =>
    `px-3 py-2 rounded-md text-sm font-medium ${
      isActive ? 'bg-brand-light text-brand-dark' : 'text-slate-600 hover:text-brand'
    }`

  return (
    <nav className="bg-white border-b border-slate-200">
      <div className="max-w-6xl mx-auto px-4 flex items-center justify-between h-14">
        <Link to="/" className="text-lg font-bold text-brand">
          CampusConnect AI
        </Link>

        <div className="flex items-center gap-1">
          <NavLink to="/" end className={linkClass}>
            Resources
          </NavLink>
          <NavLink to="/chat" className={linkClass}>
            AI Senior
          </NavLink>
          {isAdmin && (
            <NavLink to="/admin/resources" className={linkClass}>
              Manage
            </NavLink>
          )}
        </div>

        <div className="flex items-center gap-3">
          <span className="text-sm text-slate-500 hidden sm:inline">
            {user?.name}
          </span>
          <button
            onClick={handleLogout}
            className="text-sm font-medium text-slate-600 hover:text-red-600"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  )
}
