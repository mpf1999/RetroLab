const BACKEND_ORIGIN =
  import.meta.env.VITE_BACKEND_ORIGIN ??
  'http://localhost:8080'

export const resolveImageUrl = (imageUrl) => {
  if (!imageUrl) {
    return null
  }

  // if absolute URL, use directly
  if (
    imageUrl.startsWith('http://') ||
    imageUrl.startsWith('https://') ||
    imageUrl.startsWith('blob:') ||
    imageUrl.startsWith('data:')
  ) {
    return imageUrl
  }

  const normalizedPath =
    imageUrl.startsWith('/')
      ? imageUrl
      : `/${imageUrl}`

  return `${BACKEND_ORIGIN}${normalizedPath}`
}