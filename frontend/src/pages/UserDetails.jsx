import {
  useEffect,
  useState,
} from 'react'

import {
  Link,
  useNavigate,
  useParams,
} from 'react-router-dom'

import {
  ArrowLeft,
  LoaderCircle,
  Save,
  Shield,
} from 'lucide-react'

import {
  getUserById,
  updateUser,
} from '../api/userApi'

import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import ErrorMessage from '../components/ErrorMessage'
import RoleBadge from '../components/RoleBadge'
import getErrorMessage from '../utils/getErrorMessage'

import {
  formatRole,
  getInitials,
} from '../utils/userDisplayUtils'


const UserDetails = () => {
  const { userId } = useParams()

  const navigate = useNavigate()

  const [user, setUser] =
    useState(null)

  const [formData, setFormData] =
    useState({
      name: '',
      email: '',
      password: '',
      role: 'USER',
    })

  const [loading, setLoading] =
    useState(true)
  const [saving, setSaving] =
    useState(false)

  const [error, setError] =
    useState('')

  const [success, setSuccess] =
    useState('')


  // load selected user from backend and init form
  const loadUser = async () => {
    try {
      setLoading(true)
      setError('')

      const userData =
        await getUserById(userId)

      setUser(userData)

      setFormData({
        name:
          userData.name ?? '',
        email:
          userData.email ?? '',
        password: '',
        role:
          userData.role ?? 'USER',
      })
    } catch (requestError) {
      setUser(null)

      setError(
        getErrorMessage(
          requestError,
          'Could not load the user.',
        ),
      )
    } finally {
      setLoading(false)
    }
  }


  // reload the page data whenever route points to a different user
  useEffect(() => {
    loadUser()
  }, [userId])


  // generic handler
  const handleChange = (event) => {
    const { name, value } =
      event.target

    setFormData(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }


  // validate and persist
  const handleSubmit = async (
    event,
  ) => {
    event.preventDefault()

    const name =
      formData.name.trim()

    const email =
      formData.email
        .trim()
        .toLowerCase()

    if (!name || !email) {
      setError(
        'Full name and email address are required.',
      )

      return
    }
    if (!formData.password) {
      setError(
        'Enter a password to save the user.',
      )

      return
    }

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      const updatedUser =
        await updateUser(
          userId,
          {
            name,
            email,

            password:
              formData.password,

            role:
              formData.role,
          },
        )

      //replace the displayed user with the backend response and clear password
      setUser(updatedUser)

      setFormData({
        name:
          updatedUser.name ?? name,

        email:
          updatedUser.email ?? email,

        password: '',

        role:
          updatedUser.role ??
          formData.role,
      })

      setSuccess(
        'The user has been updated successfully.',
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not update the user.',
        ),
      )
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center gap-3 text-slate-500">
        <LoaderCircle className="h-7 w-7 animate-spin text-primary-500" />

        <span>Loading user...</span>
      </div>
    )
  }

  if (!user) {
    return (
      <section>
        <BackToTeam />

        <ErrorMessage
          message={error}
        />
      </section>
    )
  }


  return (
    <section>
      <BackToTeam />

      <div className="mt-6">
        <h1 className="text-3xl font-bold text-slate-900">
          User details
        </h1>

        <p className="mt-2 text-slate-500">
          View and update this team member.
        </p>
      </div>

      {error && (
        <ErrorMessage
          message={error}
        />
      )}

      {success && (
        <div
          role="status"
          className="mt-6 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700"
        >
          {success}
        </div>
      )}


      <div className="mt-8 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">

        <div className="bg-slate-900 px-8 py-8">
          <div className="flex items-center gap-5">

            <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-primary-500 text-2xl font-bold text-white">
              {getInitials(
                user.name,
                user.email,
              )}
            </div>

            <div>
              <h2 className="text-2xl font-bold text-white">
                {user.name}
              </h2>

              <p className="mt-1 text-sm text-slate-300">
                {user.email}
              </p>

              <div className="mt-3">
                <RoleBadge
                  role={user.role}
                />
              </div>
            </div>
          </div>
        </div>

        <form
          onSubmit={handleSubmit}
          className="p-8"
        >
          <div>
            <h3 className="text-lg font-semibold text-slate-900">
              Account information
            </h3>

            <p className="mt-1 text-sm text-slate-500">
              Update the member&apos;s name,
              email address or password.
            </p>
          </div>


          <div className="mt-6 grid gap-6 md:grid-cols-2">

            <FormField
              label="Full name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              disabled={saving}
              minLength={2}
              maxLength={25}
              required
            />
            <FormField
              label="Email address"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              disabled={saving}
              required
            />
            <PasswordField
              label="New password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              disabled={saving}
              minLength={8}
              maxLength={40}
              required
              autoComplete="new-password"
              placeholder="Enter a new password"
              helpText="Use between 8 and 40 characters, including an uppercase letter and a number."
            />
            <div>
              <p className={labelStyle}>
                Role
              </p>

              <div className="flex min-h-12 items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <Shield className="h-5 w-5 text-slate-400" />

                <span className="font-medium text-slate-800">
                  {formatRole(
                    user.role,
                  )}
                </span>
              </div>

              <p className="mt-2 text-xs text-slate-500">
                Change the role from the Team page.
              </p>
            </div>
          </div>

          <div className="mt-8 flex justify-end gap-3 border-t border-slate-200 pt-6">
            <button
              type="button"
              onClick={() =>
                navigate('/team')
              }
              disabled={saving}
              className="rounded-lg border border-slate-300 bg-white px-4 py-2 font-semibold text-slate-700 transition-colors hover:bg-slate-50 disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-2 rounded-lg bg-primary-500 px-4 py-2 font-semibold text-white transition-colors hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {saving ? (
                <LoaderCircle className="h-4 w-4 animate-spin" />
              ) : (
                <Save className="h-4 w-4" />
              )}

              <span>
                {saving
                  ? 'Saving...'
                  : 'Save changes'}
              </span>
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}


//shared nav link
const BackToTeam = () => {
  return (
    <Link
      to="/team"
      className="inline-flex items-center gap-2 text-sm font-semibold text-primary-600 hover:text-primary-700"
    >
      <ArrowLeft className="h-4 w-4" />

      <span>Back to Team</span>
    </Link>
  )
}


const labelStyle =
  'mb-2 block text-sm font-medium text-slate-700'


export default UserDetails
