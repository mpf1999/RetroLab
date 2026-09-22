import apiClient from './apiClient'

// prepare repair case
const normalizeRepairCase = (
  repairCase,
) => ({
  consoleId: Number(
    repairCase.consoleId,
  ),
  title: repairCase.title.trim(),

  description:
    repairCase.description?.trim() ||
    null,

  status:
    repairCase.status ?? 'OPEN',
})

// get all repair case
export const getRepairCases = async () => {
  const response = await apiClient.get(
    '/repair-cases',
  )

  return response.data
}

// get one repair case
export const getRepairCaseById = async (
  repairCaseId,
) => {
  const response = await apiClient.get(
    `/repair-cases/${repairCaseId}`,
  )
  return response.data
}

// get every repair case associated with a console
export const getRepairCasesByConsoleId =
  async (consoleId) => {
    const response = await apiClient.get(
      `/repair-cases/consoles/${consoleId}`,
    )

    return response.data
  }

// create new repair case
export const createRepairCase = async (
  repairCase,
) => {
  const response = await apiClient.post(
    '/repair-cases',
    normalizeRepairCase(repairCase),
  )

  return response.data
}

// update existing repair case
export const updateRepairCase = async (
  repairCaseId,
  repairCase,
) => {
  const response = await apiClient.put(
    `/repair-cases/${repairCaseId}`,
    normalizeRepairCase(repairCase),
  )

  return response.data
}

// delete a repair case
export const deleteRepairCase = async (
  repairCaseId,
) => {
  await apiClient.delete(
    `/repair-cases/${repairCaseId}`,
  )
}