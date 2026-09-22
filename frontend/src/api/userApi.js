import apiClient from './apiClient'

// get all users
export const getUsers = async () => {
  const response = await apiClient.get(
    '/users',
  )

  return response.data
}

// get one user
export const getUserById = async (
  userId,
) => {
  const response = await apiClient.get(
    `/users/${userId}`,
  )

  return response.data
}

// create a new user
export const createUser = async (
  userData,
) => {
  const response = await apiClient.post(
    '/users',
    {
      name: userData.name.trim(),
      email: userData.email
        .trim()
        .toLowerCase(),
      password: userData.password,
      role:
        userData.role ?? 'USER',
    },
  )

  return response.data
}

// update only role of user
export const updateUserRole = async (
  userId,
  role,
) => {
  const response = await apiClient.patch(
    `/users/${userId}/role`,
    {
      role,
    },
  )

  return response.data
}

// update only profile info
export const updateUserProfile = async (
  userId,
  profileData,
) => {
  const response = await apiClient.patch(
    `/users/${userId}/profile`,
    {
      name: profileData.name.trim(),

      email: profileData.email
        .trim()
        .toLowerCase(),
    },
  )

  return response.data
}

// performs complete user update
export const updateUser = async (
  userId,
  userData,
) => {
  const response = await apiClient.put(
    `/users/${userId}`,
    {
      name: userData.name.trim(),

      email: userData.email
        .trim()
        .toLowerCase(),
      password: userData.password,
      role: userData.role,
    },
  )

  return response.data
}

// delete user
export const deleteUser = async (
  userId,
) => {
  await apiClient.delete(
    `/users/${userId}`,
  )
}