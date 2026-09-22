import apiClient from './apiClient'

// converts an optional form value into a number, empty optional measurements are sent as null
const optionalNumber = (value) => {
  if (
    value === '' ||
    value === null ||
    value === undefined
  ) {
    return null
  }

  const numericValue = Number(value)

  // if value cannot be converted into a valid nbumber, return null, NaN not valid
  return Number.isFinite(numericValue)
    ? numericValue
    : null
}

// creates request body
const buildComponentTestRequest = (
  componentTest,
) => ({
  repairCaseId: Number(
    componentTest.repairCaseId,
  ),

  componentId: Number(
    componentTest.componentId,
  ),
  measuredVoltage: optionalNumber(
    componentTest.measuredVoltage,
  ),

  measuredCurrent: optionalNumber(
    componentTest.measuredCurrent,
  ),

  measuredResistance: optionalNumber(
    componentTest.measuredResistance,
  ),

  temperature: optionalNumber(
    componentTest.temperature,
  ),
  continuity:
    componentTest.continuity ?? null,
  result:
    componentTest.result ?? 'NOT_TESTED',
  //notes are optional
  notes:
    componentTest.notes?.trim() || null,
})

// get all component tests
export const getComponentTests =
  async () => {
    const response = await apiClient.get(
      '/component-tests',
    )

    return response.data
  }

// get one component testg
export const getComponentTestById =
  async (componentTestId) => {
    const response = await apiClient.get(
      `/component-tests/${componentTestId}`,
    )

    return response.data
  }

// get all component tests belonging to a repair case
export const getComponentTestsByRepairCaseId =
  async (repairCaseId) => {
    const response = await apiClient.get(
      `/component-tests/repair-case/${repairCaseId}`,
    )

    return response.data
  }

// retrieve all component tests
export const getComponentTestsByComponentId =
  async (componentId) => {
    const response = await apiClient.get(
      `/component-tests/component/${componentId}`,
    )

    return response.data
  }

// create new component test
export const createComponentTest =
  async (componentTest) => {
    const requestBody =
      buildComponentTestRequest(
        componentTest,
      )

    const response = await apiClient.post(
      '/component-tests',
      requestBody,
    )

    return response.data
  }

// update existinf component test
export const updateComponentTest =
  async (
    componentTestId,
    componentTest,
  ) => {
    const requestBody =
      buildComponentTestRequest(
        componentTest,
      )

    const response = await apiClient.put(
      `/component-tests/${componentTestId}`,requestBody,
    )

    return response.data
  }

// delete component test
export const deleteComponentTest =
  async (componentTestId) => {
    await apiClient.delete(
      `/component-tests/${componentTestId}`,
    )
  }