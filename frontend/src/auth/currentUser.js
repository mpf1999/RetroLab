import {
  getAccessToken,
} from './authStorage'

// decode payload inside of a JWT, only read payload
const decodeJwtPayload = (token) => {
  try {
    const parts = token.split('.')

    // header.payload.signature
    if (parts.length !== 3) {
      return null
    }
    const normalizedPayload = parts[1]
      .replaceAll('-', '+')
      .replaceAll('_', '/')

    // Base64 must have a length that is divisible by 4
    const paddedPayload =
      normalizedPayload.padEnd(
        Math.ceil(
        normalizedPayload.length / 4,
        ) * 4,
        '=',
      )

    //atob decodes Base64 into a binary string, then map converts every byte into a percentage encoded value
    const decodedPayload =
      decodeURIComponent(
        window
          .atob(paddedPayload)
          .split('')
          .map(
            (character) =>
              `%${character
                .charCodeAt(0)
                .toString(16)
                .padStart(2, '0')}`,
          )
          .join(''),
      )

    // decoded payload is json
    return JSON.parse(decodedPayload)
  } catch (error) {
    console.error(
      'Could not decode access token:',
      error,
    )

    return null
  }
}

// return email of authernticated user

export const getAuthenticatedEmail = () => {
  const token = getAccessToken()

  if (!token) {
    return null
  }
  return (
    decodeJwtPayload(token)?.sub ?? null
  )
}

// find the complete authenticated user, mail is used to identify them
export const findAuthenticatedUser = (
  users,
) => {
  // protect function
  if (!Array.isArray(users)) {
    return null
  }

  const authenticatedEmail =
    getAuthenticatedEmail()

  if (!authenticatedEmail) {
    return null
  }

  // email comparison is case insensitive
  return (
    users.find(
      (user) =>
        user.email?.toLowerCase() ===
        authenticatedEmail.toLowerCase(),
    ) ?? null
  )
}