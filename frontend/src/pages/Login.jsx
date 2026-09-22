import { useState } from 'react'
import {
  Navigate,
  useLocation,
  useNavigate,
} from 'react-router-dom'
import {
  LogIn,
  Mail,
  Wrench,
} from 'lucide-react'
import { login } from '../api/authApi'
import { isAuthenticated } from '../auth/authStorage'
import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import getErrorMessage from '../utils/getErrorMessage'

const Login = () => {
  const navigate = useNavigate()
  const location = useLocation()

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  })


  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (isAuthenticated()) {
    return <Navigate to="/" replace />
  }

  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    if (
      !formData.email.trim() ||
      !formData.password
    ) {
      setError('Email and password are required.')
      return
    }

    setLoading(true)

    try {
      await login(
        formData.email,
        formData.password,
      )

      const requestedPage =
        location.state?.from

      navigate(requestedPage || '/', {
        replace: true,
      })
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not sign in.',
        ),
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="flex min-h-screen bg-slate-100">
      <section className="hidden w-1/2 flex-col justify-between bg-slate-900 p-12 text-white lg:flex">
        <div>
          <h1 className="font-logo font-bold text-6xl">
            RetroLab
          </h1>
        </div>

        <div className="max-w-4xl">
          <h2 className="mt-6 text-6xl font-bold leading-tight">
            Repair, diagnose and manage retro consoles!
          </h2>
        </div>

        <p className="text-sm text-slate-400">
          RetroLab - A project by Manuel Pérez Feria - UOC 2026/27
        </p>
      </section>

      <section className="flex w-full items-center justify-center p-6 lg:w-1/2">
        <div className="w-full max-w-md">
          <div className="lg:hidden">
            <h1 className="font-logo text-2xl text-slate-900">
              RetroLab
            </h1>
          </div>

          <div className="mt-8 lg:mt-0">
            <h2 className="text-3xl font-bold text-slate-900">
              Welcome back
            </h2>

            <p className="mt-2 text-slate-500">
              Sign in to access your RetroLab workspace.
            </p>
          </div>

          <form
            onSubmit={handleSubmit}
            className="mt-8"
          >
            {error && (
              <div
                role="alert"
                className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-danger-500"
              >
                {error}
              </div>
            )}

            <FormField
              label="Email address"
              name="email"
              type="email"
              autoComplete="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="user@example.com"
              disabled={loading}
            />

            <div className="mt-5">
              <PasswordField
                label="Password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter your password"
                autoComplete="current-password"
                disabled={loading}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="mt-7 flex w-full items-center justify-center gap-2 rounded-lg bg-primary-500 px-4 py-3 font-semibold text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <LogIn className="h-5 w-5" />

              <span>
                {loading
                  ? 'Signing in...'
                  : 'Sign in'}
              </span>
            </button>
          </form>
        </div>
      </section>
    </main>
  )
}

export default Login