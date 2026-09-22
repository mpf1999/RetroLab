import apiClient from './apiClient'

// convert optional id into a number

const normalizeOptionalId = (value) => {
  if (
    value === '' ||
    value=== null ||
    value === undefined
  ) {
    return null
  }

  const numericId = Number(value)

  return Number.isFinite(numericId)
    ? numericId
    : null
}

// cretes json body expected by backend

const buildConsoleRequest = (
  consoleData,
) => {
  const ownerId = normalizeOptionalId(
    consoleData.ownerId,
  )

  // when ownerId exists, ownerName must be null

  const ownerName =
    ownerId === null
      ? consoleData.ownerName?.trim() ||
        null
      : null

  return {
    ownerId,
    ownerName,

    consoleModelId: Number(consoleData.consoleModelId,
    ),
    serialNumber:
      consoleData.serialNumber.trim(),

    region: consoleData.region.trim(),
    color: consoleData.color.trim(),
    condition: consoleData.condition,

    // MoneyDTO is represented embedded in console

    estimatedValue: {
      amount: Number(
        consoleData.estimatedValue.amount,
      ),
      currency:
        consoleData.estimatedValue.currency
          .trim()
          .toUpperCase(),
    },

    status: consoleData.status,

    notes:
      consoleData.notes?.trim() || null,
  }
}

// get every console
export const getConsoles = async () => {
  const response = await apiClient.get(
    '/consoles',
  )

  return response.data
}

// Get one console
export const getConsoleById = async (
  consoleId,
) => {
  const response = await apiClient.get(
    `/consoles/${consoleId}`,
  )

  return response.data
}

// Upload or replace a console image, multipart as its a binary file, not json

export const uploadConsoleImage = async (
  consoleId,
  image,
) => {
  // if a valid file does not exist, preserve the function's expected return type

  if (!(image instanceof File)) {
    return getConsoleById(consoleId)
  }

  const formData = new FormData()

  // image must match @RequestPart name in ConsoleController

  formData.append('image', image)

  const response = await apiClient.post(
    `/consoles/${consoleId}/image`,
    formData,
  )
  return response.data
}

// Create console and optionally upload image, two requests for two separate endpoints

export const createConsole = async (
  consoleData,
  image = null,
) => {
  const createResponse =
    await apiClient.post(
      '/consoles',
      buildConsoleRequest(consoleData),
    )

  const createdConsole =
    createResponse.data
  if (!(image instanceof File)) {
    return createdConsole
  }

  return uploadConsoleImage(
    createdConsole.consoleId,
    image,
  )
}

// update console and optionally replace image
export const updateConsole = async (
  consoleId,
  consoleData,
  image = null,
) => {
  const updateResponse =
    await apiClient.put(
      `/consoles/${consoleId}`,
      buildConsoleRequest(consoleData),
    )

  const updatedConsole =
    updateResponse.data

  if (!(image instanceof File)) {
    return updatedConsole
  }

  return uploadConsoleImage(
    consoleId,
    image,
  )
}

// delete image
export const removeConsoleImage = async (
  consoleId,
) => {
  const response = await apiClient.delete(
    `/consoles/${consoleId}/image`,
  )

  return response.data
}

// delete console
export const deleteConsole = async (
  consoleId,
) => {
  await apiClient.delete(
    `/consoles/${consoleId}`,
  )
}