import {
  useEffect,
  useState,
} from 'react'

import {
  LoaderCircle,
  Mail,
  Pencil,
  RefreshCw,
  Save,
  Shield,
  X,
} from 'lucide-react'

import {
  getUsers,
  updateUserProfile,
} from '../api/userApi'

import {
  findAuthenticatedUser,
} from '../auth/currentUser'

import FormField from '../components/FormField'
import ErrorMessage from '../components/ErrorMessage'
import RoleBadge from '../components/RoleBadge'
import getErrorMessage from '../utils/getErrorMessage'

import {
  formatRole,
  getInitials,
  getUserId,
} from '../utils/userDisplayUtils'

const Profile = () => {
  const [user, setUser] =
    useState(null)

  const [formData, setFormData] =
    useState({
      name: '',
      email: '',
    })

  const [loading, setLoading] =
    useState(true)
  const [saving, setSaving] =
    useState(false)
  const [isEditing, setIsEditing] =useState(false)
  const [error, setError] =
    useState('')
  const [success, setSuccess] =useState('')

  // load users from backend and identify authenticated one
  const loadProfile = async () => {
    try {
      setLoading(true)
      setError('')
      setSuccess('')

      const usersData = await getUsers()

      const users = Array.isArray(usersData)
        ? usersData
        : []

      const authenticatedUser =
        findAuthenticatedUser(users)
      if (!authenticatedUser) {
        throw new Error(
          'The authenticated user could not be found.',
        )
      }

      setUser(authenticatedUser)

      setFormData({
        name:
          authenticatedUser.name ?? '',

        email:
          authenticatedUser.email ?? '',
      })
    } catch (requestError) {
      setUser(null)

      setError(
        getErrorMessage(
          requestError,
          'Could not load your profile.',
        ),
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadProfile()
  }, [])

  // generic handler for editable profile fields
  const handleChange = (event) => {
    const { name, value } =
      event.target
    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }
  const handleEdit = () => {
    setFormData({
      name: user.name ?? '',
      email: user.email ?? '',
    })

    setError('')
    setSuccess('')
    setIsEditing(true)
  }

  // cancel editing and restore
  const handleCancel = () => {
    setFormData({
      name: user.name ?? '',
      email: user.email ?? '',
    })

    setError('')
    setSuccess('')
    setIsEditing(false)
  }
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

    const userId = getUserId(user)

    try {
      setSaving(true)
      setError('')
      setSuccess('')

      const updatedUser =
        await updateUserProfile(
          userId,
          {
            name,
            email,
          },
        )

      const nextUser = {
        ...user,
        ...(updatedUser ?? {}),

        name:
          updatedUser?.name ??
          name,

        email:
          updatedUser?.email ??
          email,

        role:
          updatedUser?.role ??
          user.role,
      }

      setUser(nextUser)

      setFormData({
        name: nextUser.name,
        email: nextUser.email,
      })

      // notify navbar to refresh
      window.dispatchEvent(
        new CustomEvent(
          'retrolab:user-updated',
          {
            detail: nextUser,
          },
        ),
      )

      setIsEditing(false)

      setSuccess(
        'Your profile has been updated successfully.',
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not update your profile.',
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

        <span>Loading profile...</span>
      </div>
    )
  }

  // error
  if (!user) {
    return (
      <section>
        <ProfileHeader />

        <ErrorMessage message={error}>
          <button
            type="button"
            onClick={loadProfile}
            className="mt-3 flex items-center gap-2 font-semibold"
          >
            <RefreshCw className="h-4 w-4" />

            <span>Try again</span>
          </button>
        </ErrorMessage>
      </section>
    )
  }

  // email as fallback if no name
  const displayName =
    user.name?.trim() ||
    user.email

  const roleName = formatRole(user.role)

  return (
    <section>
      <ProfileHeader />

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
          <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
            <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-full bg-primary-500 text-3xl font-bold text-white shadow-lg">
              {getInitials(user.name, user.email)}
            </div>

            <div>
              <h2 className="text-2xl font-bold text-white">
                {displayName}
              </h2>

              <div className="mt-2 flex items-center gap-2 text-sm text-slate-300">
                <Mail className="h-4 w-4" />

                <span>{user.email}</span>
              </div>

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
          <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <h3 className="text-lg font-semibold text-slate-900">
                Personal information
              </h3>

              <p className="mt-1 text-sm text-slate-500">
                Manage your name and email address.
              </p>
            </div>

            {!isEditing && (
              <button
                type="button"
                onClick={handleEdit}
                className="flex items-center justify-center gap-2 rounded-lg bg-primary-500 px-4 py-2 font-semibold text-white transition-colors hover:bg-primary-600"
              >
                <Pencil className="h-4 w-4" />

                <span>Edit profile</span>
              </button>
            )}
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <div>
              <FormField
                label="Full name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                disabled={!isEditing || saving}
                required
              />
            </div>


            <div>
              <FormField
                label="Email address"
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                disabled={!isEditing || saving}
                required
              />
            </div>


            <div>
              <p className="mb-2 text-sm font-medium text-slate-700">
                Role
              </p>

              <div className="flex min-h-12 items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                <Shield className="h-5 w-5 text-slate-400" />

                <span className="font-medium text-slate-800">
                  {roleName}
                </span>
              </div>

              <p className="mt-2 text-xs text-slate-500">
                Roles are managed from the Team page.
              </p>
            </div>
          </div>


          {isEditing && (
            <div className="mt-8 flex justify-end gap-3 border-t border-slate-200 pt-6">
              <button
                type="button"
                onClick={handleCancel}
                disabled={saving}
                className="flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2 font-semibold text-slate-700 transition-colors hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
              >
                <X className="h-4 w-4" />

                <span>Cancel</span>
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
          )}
        </form>
      </div>
    </section>
  )
}

// static page heading

const ProfileHeader = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold text-slate-900">
        My Profile
      </h1>

      <p className="mt-2 text-slate-500">
        View and manage your RetroLab account.
      </p>
    </div>
  )
}


export default Profile