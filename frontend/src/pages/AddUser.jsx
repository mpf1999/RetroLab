import { useState } from 'react'
import {
  Link,
  useNavigate,
} from 'react-router-dom'
import {
  AlertCircle,
  ArrowLeft,
  LoaderCircle,
  Save,
  UserPlus,
} from 'lucide-react'

import { createUser } from '../api/userApi'
import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import getErrorMessage from '../utils/getErrorMessage'

const AddUser = () => {
  const navigate = useNavigate()

  const [formData, setFormData] =
    useState({
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      role: 'USER',
    })

  const [submitting, setSubmitting] =
    useState(false)

  const [error, setError] = useState('')

  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))

    if (error) {
      setError('')
    }
  }

  const validateForm = () => {
    if (!formData.name.trim()) {
      return 'Name is required.'
    }

    if (!formData.email.trim()) {
      return 'Email is required.'
    }

    if (!formData.password) {
      return 'Password is required.'
    }

    if (formData.password.length < 8) {
      return 'Password must contain at least 8 characters.'
    }

    const passwordPattern =
      /^(?=.*[A-Z])(?=.*\d).*$/

    if (
      !passwordPattern.test(
        formData.password,
      )
    ) {
      return 'Password must contain at least one uppercase letter and one number.'
    }

    if (
      formData.password !==
      formData.confirmPassword
    ) {
      return 'Passwords do not match.'
    }

    return null
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    const validationError =
      validateForm()

    if (validationError) {
      setError(validationError)
      return
    }

    try {
      setSubmitting(true)
      setError('')

      await createUser({
        name: formData.name.trim(),
        email: formData.email
          .trim()
          .toLowerCase(),
        password: formData.password,
        role: formData.role,
      })

      navigate('/team', {
        replace: true,
      })
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not create the user.',
        ),
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="mx-auto max-w-3xl">
      <Link
        to="/team"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to team
      </Link>

      <div className="mt-6">
        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary-100 text-primary-600">
            <UserPlus className="h-6 w-6" />
          </div>

          <div>
            <h1 className="text-3xl font-bold text-slate-900">
              Add Team Member
            </h1>

            <p className="mt-1 text-slate-500">
              Create access credentials for a new RetroLab member.
            </p>
          </div>
        </div>
      </div>

      {error && (
        <div
          role="alert"
          className="mt-6 flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700"
        >
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        className="mt-8 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
      >
        <fieldset
          disabled={submitting}
          className="disabled:opacity-70"
        >
          <div className="p-6">
            <h2 className="text-lg font-semibold text-slate-900">
              Member information
            </h2>

            <div className="mt-6 grid gap-6 sm:grid-cols-2">
              <FormField
                label="Full name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
                maxLength={100}
                autoComplete="name"
              />

              <FormField
                label="Email address"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                required
                autoComplete="email"
              />

              <FormField
                label="Role"
                name="role"
                value={formData.role}
                onChange={handleChange}
                required
              >
                <option value="USER">
                  Technician
                </option>

                <option value="ADMIN">
                  Administrator
                </option>
              </FormField>

              <div className="sm:col-span-2">
                <PasswordField
                  label="Password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  minLength={8}
                  pattern="(?=.*[A-Z])(?=.*\d).*"
                  title="Password must contain at least one uppercase letter and one number."
                  autoComplete="new-password"
                  helpText="Use at least 8 characters, including one uppercase letter and one number."
                />
              </div>

              <div className="sm:col-span-2">
                <PasswordField
                  label="Confirm password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  required
                  minLength={8}
                  pattern="(?=.*[A-Z])(?=.*\d).*"
                  title="Password must contain at least one uppercase letter and one number."
                  autoComplete="new-password"
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-200 bg-slate-50 px-6 py-4">
            <Link
              to="/team"
              className="rounded-lg border border-slate-300 bg-white px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </Link>

            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {submitting ? (
                <LoaderCircle className="h-5 w-5 animate-spin" />
              ) : (
                <Save className="h-5 w-5" />
              )}

              {submitting
                ? 'Creating...'
                : 'Create member'}
            </button>
          </div>
        </fieldset>
      </form>
    </section>
  )
}

export default AddUser