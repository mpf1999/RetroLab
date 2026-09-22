export const getUserId = (user) => {
  return user?.userId ?? user?.id
}

export const sameUserId = (firstId, secondId) => {
  if (
    firstId === null ||
    firstId === undefined ||
    secondId === null ||
    secondId === undefined
  ) {
    return false
  }

  return String(firstId) === String(secondId)
}

export const normalizeUser = (user) => {
  const userId = getUserId(user)

  return {
    ...user,
    userId,
    name:
      user?.name?.trim() ||
      user?.email ||
      `User ${userId}`,
  }
}

export const getInitials = (name, email) => {
  const source =
    name?.trim() ||
    email?.trim()

  if (!source) {
    return '?'
  }

  const parts = source
    .split(/\s+/)
    .filter(Boolean)

  if (parts.length === 1) {
    return parts[0]
      .slice(0, 2)
      .toUpperCase()
  }

  return (
    parts[0][0] +
    parts[parts.length - 1][0]
  ).toUpperCase()
}

export const formatRole = (role) => {
  return role === 'ADMIN'
    ? 'Administrator'
    : 'Technician'
}