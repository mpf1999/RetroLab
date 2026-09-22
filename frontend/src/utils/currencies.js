const PREFERRED_CURRENCY_KEY =
  'preferredCurrency'

export const currencyCodes =
  Intl.supportedValuesOf('currency')

const currencyNames =
  new Intl.DisplayNames(
    [navigator.language, 'en'],
    {
      type: 'currency',
    },
  )

export const getCurrencyLabel = (
  currencyCode,
) => {
  const name =
    currencyNames.of(currencyCode)

  return `${currencyCode} — ${name}`
}

export const getPreferredCurrency = () => {
  const savedCurrency =
    localStorage.getItem(
      PREFERRED_CURRENCY_KEY,
    )

  if (
    savedCurrency &&
    currencyCodes.includes(savedCurrency)
  ) {
    return savedCurrency
  }

  return 'EUR'
}

export const savePreferredCurrency = (
  currencyCode,
) => {
  if (
    !currencyCodes.includes(currencyCode)
  ) {
    return false
  }

  localStorage.setItem(
    PREFERRED_CURRENCY_KEY,
    currencyCode,
  )

  return true
}