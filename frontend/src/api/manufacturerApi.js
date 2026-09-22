import apiClient from './apiClient'

// get all manufacturers
export const getManufacturers = async () => {
  const response = await apiClient.get(
    '/manufacturers',
  )

  return response.data
}

// get a manufacturer
export const getManufacturerById = async (
  manufacturerId,
) => {
  const response = await apiClient.get(
    `/manufacturers/${manufacturerId}`,
  )

  return response.data
}

//create request body for backend
const buildManufacturerRequest = (
  manufacturerData,
) => ({
  manufacturerName:
    manufacturerData.manufacturerName
      .trim(),
  countryCode:
    manufacturerData.countryCode
      .trim()
      .toUpperCase(),
})

// create new manufacturer
export const createManufacturer = async (
  manufacturerData,
) => {
  const requestBody =
    buildManufacturerRequest(
      manufacturerData,
    )

  const response = await apiClient.post(
    '/manufacturers',
    requestBody,
  )

  return response.data
}

// update existing manufacturer
export const updateManufacturer = async (
  manufacturerId,
  manufacturerData,
) => {
  const requestBody =
    buildManufacturerRequest(
      manufacturerData,
    )

  const response = await apiClient.put(
    `/manufacturers/${manufacturerId}`,
    requestBody,
  )

  return response.data
}

// delete manufacturer
export const deleteManufacturer = async (
  manufacturerId,
) => {
  await apiClient.delete(
    `/manufacturers/${manufacturerId}`,
  )
}