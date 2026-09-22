import apiClient from './apiClient'

// get all console models
export const getConsoleModels = async () => {
  const response = await apiClient.get(
    '/console-models',
  )

  return response.data
}

// get one console model by id
export const getConsoleModelById = async (
  consoleModelId,
) => {
  const response = await apiClient.get(
    `/console-models/${consoleModelId}`,
  )

  return response.data
}

// creates request body expected by backend
const buildConsoleModelRequest = (
  modelData,
) => ({

  consoleModelName:
    modelData.consoleModelName.trim(),
  releaseYear: Number(
    modelData.releaseYear,
  ),

  manufacturerId: Number(
    modelData.manufacturerId,
  ),
})

// create new console model
export const createConsoleModel = async (
  modelData,
) => {
  const requestBody =
    buildConsoleModelRequest(modelData)

  const response = await apiClient.post(
    '/console-models',
    requestBody,
  )

  return response.data
}
//update existing console model
export const updateConsoleModel = async (
  consoleModelId,
  modelData,
) => {
  const requestBody =
    buildConsoleModelRequest(modelData)

  const response = await apiClient.put(
    `/console-models/${consoleModelId}`,
    requestBody,
  )

  return response.data
}

// delete console model
export const deleteConsoleModel = async (
  consoleModelId,
) => {
  await apiClient.delete(
    `/console-models/${consoleModelId}`,
  )
}