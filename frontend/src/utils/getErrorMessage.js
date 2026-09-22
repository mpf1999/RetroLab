const getErrorMessage = (
  error,
  fallbackMessage,
) => {
  const validationErrors =
    error.response?.data?.errors

  if (
    Array.isArray(validationErrors) &&
    validationErrors.length > 0
  ) {
    return validationErrors.join(' ')
  }

  return (
    error.userMessage ??
    error.response?.data?.message ??
    error.message ??
    fallbackMessage
  )
}

export default getErrorMessage
