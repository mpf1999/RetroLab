// Keys used to store authentication data in localStorage

const TOKEN_KEY = 'retrolab_token'

const EXPIRATION_KEY =
  'retrolab_token_expiration'

//Removes every authentication value stored in the browser, used when logged out or JWT expires

export const clearAuthentication = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(EXPIRATION_KEY)
}

// reads and validates expiration in localStorage, must be converted back into a number

const getStoredExpiration = () => {
  const storedExpiration =
    localStorage.getItem(EXPIRATION_KEY)

  if (!storedExpiration) {
    return null
  }

  const expirationTime =Number(storedExpiration)

  if (
    !Number.isFinite(expirationTime) ||
    expirationTime <= 0
  ) {
    return null
  }

  return expirationTime
}

// saves the JWT returned by the login endpoint

export const saveAuthentication = ({
  token,
  expiresIn,
}) => {
  if (
    typeof token !== 'string' ||
    !token.trim()
  ) {
    throw new Error(
      'The authentication response does not contain a token.',
    )
  }

  const durationInMilliseconds = Number(expiresIn)

  if (
    !Number.isFinite(
      durationInMilliseconds,
    ) ||durationInMilliseconds <= 0
  ) {
    throw new Error(
      'The authentication response contains an invalid expiration time.',
    )
  }

  // convert into an absolute expiration timestamp

  const expirationTime =Date.now() + durationInMilliseconds

  localStorage.setItem(
    TOKEN_KEY,
    token.trim(),
  )

  localStorage.setItem(
    EXPIRATION_KEY,
    String(expirationTime),
  )
}

// return JWT only if it exists and not expired

export const getAccessToken = () => {
  const token =
    localStorage.getItem(TOKEN_KEY)

  const expirationTime =
    getStoredExpiration()

  if (!token || !expirationTime) {
    clearAuthentication()
    return null
  }

  if (Date.now() >= expirationTime) {
    clearAuthentication()
    return null
  }

  return token
}

export const getTokenExpiration = () => {
  return getStoredExpiration()
}

// Thhe user is considered authenticated only when a valid token exists, if its not expired

export const isAuthenticated = () => {
  return Boolean(getAccessToken())
}