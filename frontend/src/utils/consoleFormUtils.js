const validateConsoleForm = (formData) => {
  if (!formData.consoleModelId) return 'Select a console model.'
  if (!formData.serialNumber.trim()) return 'Serial number is required.'
  if (!formData.region.trim()) return 'Region is required.'
  if (!formData.color.trim()) return 'Color is required.'
  if (!formData.condition) return 'Condition is required.'
  if (!formData.status) return 'Status is required.'

  if (
    formData.ownerType === 'TEAM_MEMBER' &&
    !formData.ownerId
  ) {
    return 'Select a team member.'
  }

  if (
    formData.ownerType === 'CLIENT' &&
    !formData.ownerName.trim()
  ) {
    return 'Client name is required.'
  }

  if (formData.estimatedValue.amount === '') {
    return 'Estimated value is required.'
  }

  const amount = Number(formData.estimatedValue.amount)

  if (!Number.isFinite(amount) || amount < 0) {
    return 'Enter a valid estimated value.'
  }

  if (!formData.estimatedValue.currency) {
    return 'Select a currency.'
  }

  return null
}

const buildConsoleRequest = (formData) => ({
  ownerId:
    formData.ownerType === 'TEAM_MEMBER'
      ? Number(formData.ownerId)
      : null,
  ownerName:
    formData.ownerType === 'CLIENT'
      ? formData.ownerName.trim()
      : null,
  consoleModelId: Number(formData.consoleModelId),
  serialNumber: formData.serialNumber.trim(),
  region: formData.region.trim(),
  color: formData.color.trim(),
  condition: formData.condition,
  status: formData.status,
  estimatedValue: {
    amount: Number(formData.estimatedValue.amount),
    currency: formData.estimatedValue.currency,
  },
  notes: formData.notes.trim() || null,
})

export { buildConsoleRequest, validateConsoleForm }