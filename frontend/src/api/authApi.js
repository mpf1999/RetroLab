import apiClient from './apiClient'

import {
  clearAuthentication,
  saveAuthentication,
} from '../auth/authStorage'

// Send the user credentials to the backend

export const login = async (
  email,
  password,
) => {
  // perform a small validation before sending request

  if (
    typeof email !== 'string' ||
    !email.trim()
  ) {
    throw new Error(
      'Email is required.',
    )
  }

  if (
    typeof password !== 'string' ||
    !password
  ) {
    throw new Error(
      'Password is required.',
    )
  }

  // Axios converts this js object into a json request automatically

  const response = await apiClient.post(
    '/auth/login',
    {
      email: email
        .trim()
        .toLowerCase(),

      password,
    },
  )

  // saveAuthentication saves token and expiration time in localStorage after validation

  saveAuthentication({
    token: response.data.token,
    expiresIn: response.data.expiresIn,
  })

  return response.data
}

// log out, no session needs to be deleted as JWT is stateless

export const logout = () => {
  clearAuthentication()
}