import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  Link,
} from 'react-router-dom'

import {
  AlertCircle,
  LoaderCircle,
  Mail,
  Pencil,
  RefreshCw,
  Trash2,
  UserPlus,
  UserRound,
  Users,
} from 'lucide-react'

import {
  deleteUser,
  getUsers,
  updateUserRole,
} from '../api/userApi'

import {
  findAuthenticatedUser,
} from '../auth/currentUser'

import ErrorMessage from '../components/ErrorMessage'
import SearchField from '../components/SearchField'
import RoleBadge from '../components/RoleBadge'
import getErrorMessage from '../utils/getErrorMessage'
import {
  getUserId,
  normalizeUser,
  sameUserId,
} from '../utils/userDisplayUtils'

const Team = () => {
  const [members, setMembers] =
    useState([])

  const [searchTerm, setSearchTerm] =
    useState('')

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [
    updatingMemberIds,
    setUpdatingMemberIds,
  ] = useState([])

  const [
    deletingMemberIds,
    setDeletingMemberIds,
  ] = useState([])

  // load all users from backend
  const loadMembers = async () => {
    try {
      setLoading(true)
      setError('')

      const usersData =
        await getUsers()

      setMembers(
        Array.isArray(usersData)
          ? usersData
          : [],
      )
    } catch (requestError) {
      setMembers([])

      setError(
        getErrorMessage(
          requestError,
          'Could not load team members.',
        ),
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadMembers()
  }, [])

  // resolve authenticated user by JWT
  const currentUser = useMemo(
    () =>
      findAuthenticatedUser(
        members,
      ),
    [members],
  )

  const currentUserId =
    getUserId(currentUser)

  const isAdmin =
    currentUser?.role === 'ADMIN'

  const normalizedMembers =
    useMemo(() => {
      return members
        .map(normalizeUser)
        .filter(
          (member) =>
            member.userId !== null &&
            member.userId !== undefined,
        )
        .sort(
          (firstMember, secondMember) =>
            Number(firstMember.userId) -
            Number(secondMember.userId),
        )
    }, [members])

  // filter team members locally
  const filteredMembers =
    useMemo(() => {
      const search =
        searchTerm
          .trim()
          .toLowerCase()

      if (!search) {
        return normalizedMembers
      }

      return normalizedMembers.filter(
        (member) => {
          const searchableText = [
            member.name,
            member.email,
            member.role,
          ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()

          return searchableText.includes(
            search,
          )
        },
      )
    }, [
      normalizedMembers,
      searchTerm,
    ])

  // change another user´s role, only for admins
  const handleRoleChange = async (
    member,
    newRole,
  ) => {
    if (!isAdmin) {
      setError(
        'Only administrators can change user roles.',
      )

      return
    }

    const memberId =
      getUserId(member)

    if (
      sameUserId(memberId, currentUserId)
    ) {
      setError(
        'You cannot change your own role.',
      )

      return
    }

    const previousRole =
      member.role

    setError('')

    setUpdatingMemberIds(
      (currentIds) => [
        ...currentIds,
        memberId,
      ],
    )

    //Apply the new role optimistically so the interface responds immediately whiile the backend request is in progress
    setMembers(
      (currentMembers) =>
        currentMembers.map(
          (currentMember) => {
            const currentMemberId =
              getUserId(currentMember)

            if (
              !sameUserId(currentMemberId, memberId)
            ) {
              return currentMember
            }

            return {
              ...currentMember,
              role: newRole,
            }
          },
        ),
    )

    try {
      const updatedUser =
        await updateUserRole(
          memberId,
          newRole,
        )

      setMembers(
        (currentMembers) =>
          currentMembers.map(
            (currentMember) => {
              const currentMemberId =
                getUserId(currentMember)

              if (
                !sameUserId(currentMemberId, memberId)
              ) {
                return currentMember
              }

              return {
                ...currentMember,
                ...(updatedUser ??
                  {}),

                role:
                  updatedUser?.role ??
                  newRole,
              }
            },
          ),
      )
    } catch (requestError) {
      setMembers(
        (currentMembers) =>
          currentMembers.map(
            (currentMember) => {
              const currentMemberId =
                getUserId(currentMember)

              if (
                !sameUserId(currentMemberId, memberId)
              ) {
                return currentMember
              }

              return {
                ...currentMember,
                role: previousRole,
              }
            },
          ),
      )
      setError(
        getErrorMessage(
          requestError,
          'Could not update the user role.',
        ),
      )
    } finally {
      setUpdatingMemberIds(
        (currentIds) =>
          currentIds.filter(
            (currentId) =>
              String(currentId) !==
              String(memberId),
          ),
      )
    }
  }

  // delete another team member after confirmation
  const handleDeleteMember =
    async (member) => {
      if (!isAdmin) {
        setError(
          'Only administrators can delete users.',
        )

        return
      }

      const memberId =
        getUserId(member)

      if (
        sameUserId(memberId, currentUserId)
      ) {
        setError(
          'You cannot delete your own account.',
        )

        return
      }

      const confirmed =
        window.confirm(
          `Delete ${member.name}? This action cannot be undone.`,
        )

      if (!confirmed) {
        return
      }

      try {
        setError('')

        setDeletingMemberIds(
          (currentIds) => [
            ...currentIds,
            memberId,
          ],
        )

        await deleteUser(memberId)

        setMembers(
          (currentMembers) =>
            currentMembers.filter(
              (currentMember) => {
                const currentMemberId =
                  getUserId(currentMember)

                return (
                  !sameUserId(currentMemberId, memberId)
                )
              },
            ),
        )
      } catch (requestError) {
        setError(
          getErrorMessage(
            requestError,
            'Could not delete the user.',
          ),
        )
      } finally {
        setDeletingMemberIds(
          (currentIds) =>
            currentIds.filter(
              (currentId) =>
                !sameUserId(currentId, memberId),
            ),
        )
      }
    }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center gap-3 text-slate-500">
        <LoaderCircle className="h-7 w-7 animate-spin text-primary-500" />

        <span>
          Loading team members...
        </span>
      </div>
    )
  }

  return (
    <section>
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">
            Team
          </h1>

          <p className="mt-2 text-slate-500">
            View and manage the members
            who have access to RetroLab.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 rounded-lg bg-primary-50 px-4 py-2 text-primary-700">
            <Users className="h-5 w-5" />

            <span className="font-medium">
              {
                normalizedMembers.length
              }{' '}
              {normalizedMembers.length ===
              1
                ? 'member'
                : 'members'}
            </span>
          </div>

          {isAdmin && (
            <Link
              to="/team/new"
              className="flex items-center gap-2 rounded-lg bg-primary-500 px-4 py-2 font-semibold text-white transition-colors hover:bg-primary-600"
            >
              <UserPlus className="h-5 w-5" />
              <span>
                Add member
              </span>
            </Link>
          )}
        </div>
      </div>

      {error && (
        <ErrorMessage message={error}>
          {members.length === 0 && (
            <button
              type="button"
              onClick={loadMembers}
              className="mt-3 flex items-center gap-2 font-semibold"
            >
              <RefreshCw className="h-4 w-4" />
              <span>Try again</span>
            </button>
          )}
        </ErrorMessage>
      )}
      {!currentUser &&
        members.length > 0 && (
          <div className="mt-6 flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
            <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />

            <p>
              The authenticated user
              could not be matched with
              a team member. User
              management has been
              disabled.
            </p>
          </div>
        )}

      <div className="mt-8 max-w-md">
        <SearchField
          value={searchTerm}
          onChange={(value) =>
            setSearchTerm(value)
          }
          placeholder="Search team members..."
        />
      </div>

      <div className="mt-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th
                  className={
                    tableHeadingStyle
                  }
                >
                  Member
                </th>
                <th
                  className={
                    tableHeadingStyle
                  }
                >
                  Email
                </th>
                <th
                  className={
                    tableHeadingStyle
                  }
                >
                  Role
                </th>
                {isAdmin && (
                  <th
                    className={
                      tableHeadingStyle
                    }
                  >
                    Actions
                  </th>
                )}
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-200">
              {filteredMembers.map(
                (member) => {
                  const memberId =
                    member.userId

                  const isCurrentUser =
                    sameUserId(memberId, currentUserId)

                  const updating =
                    updatingMemberIds.some(
                      (
                        updatingId,
                      ) =>
                        sameUserId(updatingId, memberId),
                    )

                  const deleting =
                    deletingMemberIds.some(
                      (
                        deletingId,
                      ) =>
                        sameUserId(deletingId, memberId),
                    )

                  return (
                    <tr
                      key={memberId}
                      className="transition-colors hover:bg-slate-50"
                    >
                      {/* Member identity. */}
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-200 text-slate-600">
                            <UserRound className="h-5 w-5" />
                          </div>

                          <div>
                            <Link
                              to={`/team/${memberId}`}
                              className="font-medium text-slate-900 transition-colors hover:text-primary-600"
                            >
                              {
                                member.name
                              }
                            </Link>

                            {isCurrentUser && (
                              <span className="ml-2 text-xs font-normal text-slate-400">
                                You
                              </span>
                            )}
                          </div>
                        </div>
                      </td>

                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2 text-sm text-slate-600">
                          <Mail className="h-4 w-4 shrink-0 text-slate-400" />

                          <span>
                            {
                              member.email
                            }
                          </span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <RoleBadge
                          role={
                            member.role
                          }
                        />
                      </td>

                      {isAdmin && (
                        <td className="px-6 py-4">
                          <div className="flex flex-wrap items-center gap-3">

                            <Link
                              to={`/team/${memberId}`}
                              aria-label={`Edit ${member.name}`}
                              title={`Edit ${member.name}`}
                              className="flex h-10 w-10 items-center justify-center rounded-lg border border-slate-300 bg-white text-slate-600 transition-colors hover:border-primary-300 hover:bg-primary-50 hover:text-primary-600"
                            >
                              <Pencil className="h-4 w-4" />
                            </Link>
                            <select
                              value={
                                member.role
                              }
                              onChange={(
                                event,
                              ) =>
                                handleRoleChange(
                                  member,
                                  event
                                    .target
                                    .value,
                                )
                              }
                              disabled={
                                isCurrentUser ||
                                updating ||
                                deleting
                              }
                              aria-label={`Change role for ${member.name}`}
                              className="rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-400"
                            >
                              <option value="USER">
                                Technician
                              </option>

                              <option value="ADMIN">
                                Administrator
                              </option>
                            </select>

                            {updating && (
                              <LoaderCircle className="h-4 w-4 animate-spin text-primary-500" />
                            )}
                            <button
                              type="button"
                              onClick={() =>
                                handleDeleteMember(
                                  member,
                                )
                              }
                              disabled={
                                isCurrentUser ||
                                updating ||
                                deleting
                              }
                              aria-label={`Delete ${member.name}`}
                              title={
                                isCurrentUser
                                  ? 'You cannot delete your own account'
                                  : `Delete ${member.name}`
                              }
                              className="flex h-10 w-10 items-center justify-center rounded-lg border border-red-200 bg-white text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-40"
                            >
                              {deleting ? (
                                <LoaderCircle className="h-4 w-4 animate-spin" />
                              ) : (
                                <Trash2 className="h-4 w-4" />
                              )}
                            </button>
                          </div>

                          {isCurrentUser && (
                            <p className="mt-1 text-xs text-slate-400">
                              You cannot change your own role or delete your own account.
                            </p>
                          )}
                        </td>
                      )}
                    </tr>
                  )
                },
              )}
            </tbody>
          </table>
        </div>
        {filteredMembers.length ===
          0 && (
          <div className="p-10 text-center text-slate-500">
            No team members were found.
          </div>
        )}
      </div>

      {!isAdmin &&
        currentUser && (
          <p className="mt-4 text-sm text-slate-500">
            Only administrators can create, delete or change the role of team members.
          </p>
        )}
    </section>
  )
}


const tableHeadingStyle =
  'px-6 py-4 text-sm font-semibold text-slate-600'

export default Team