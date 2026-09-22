import apiClient from './apiClient'

// get all compontents
export const getComponents = async () => {
  const response = await apiClient.get(
    '/components',
  )

  return response.data
}

// get one component by id
export const getComponentById = async (
  componentId,
) => {
  const response = await apiClient.get(
    `/components/${componentId}`,
  )

  return response.data
}

// get the components belonging to a console model
export const getComponentsByConsoleModelId =
  async (consoleModelId) => {
    const components =
      await getComponents()

    const numericConsoleModelId = Number(consoleModelId)

    // prevent comparison between 1 and '1'
    return components.filter(
      (component) =>
        Number(component.consoleModelId) ===numericConsoleModelId,
    )
  }

//create request body expected by backend
const buildComponentRequest = (
  componentData,
) => ({

  consoleModelId: Number(
    componentData.consoleModelId,
  ),
  name: componentData.name.trim(),
  description:
    componentData.description?.trim() ||
    null,
})

// create new component
export const createComponent = async (
  componentData,
) => {
  const requestBody =
    buildComponentRequest(componentData)

  const response = await apiClient.post(
    '/components',
    requestBody,
  )

  return response.data
}

// update existing component
export const updateComponent = async (
  componentId,
  componentData,
) => {
  const requestBody =
    buildComponentRequest(componentData)

  const response = await apiClient.put(
    `/components/${componentId}`,
    requestBody,
  )

  return response.data
}

//delete component and dependent elements
export const deleteComponent = async (
  componentId,
) => {
  await apiClient.delete(
    `/components/${componentId}`,
  )
}