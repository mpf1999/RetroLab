//values must match the Condition enum declared in the Spring Boot backend

export const CONSOLE_CONDITIONS = [
  {
    value: 'EXCELLENT',
    label: 'Excellent',
  },
  {
    value: 'GOOD',
    label: 'Good',
  },
  {
    value: 'FAIR',
    label: 'Fair',
  },
  {
    value: 'POOR',
    label: 'Poor',
  },
  {
    value: 'BROKEN',
    label: 'Broken',
  },
]

//values must match the Status enum declared in the Spring Boot backend
export const CONSOLE_STATUSES = [
  {
    value: 'AVAILABLE',
    label: 'Available',
  },
  {
    value: 'IN_REPAIR',
    label: 'In repair',
  },
  {
    value: 'REPAIRED',
    label: 'Repaired',
  },
  {
    value: 'RETURNED',
    label: 'Returned',
  },
  {
    value: 'SOLD',
    label: 'Sold',
  },
  {
    value: 'ARCHIVED',
    label: 'Archived',
  },
]