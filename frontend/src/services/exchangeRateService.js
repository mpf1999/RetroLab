const API_URL = 'https://api.frankfurter.dev/v2'
const CACHE_KEY = 'retrolab_exchange_rates_eur'

// Las cotizaciones se renuevan cada 12 horas
const CACHE_DURATION = 12 * 60 * 60 * 1000

const readCache = () => {
  try {
    const storedValue = localStorage.getItem(CACHE_KEY)

    if (!storedValue) {
      return null
    }

    return JSON.parse(storedValue)
  } catch {
    return null
  }
}

const cacheIsValid = (cache) => {
  if (!cache?.fetchedAt || !cache?.rates) {
    return false
  }

  return Date.now() - cache.fetchedAt < CACHE_DURATION
}

const saveCache = (data) => {
  localStorage.setItem(
    CACHE_KEY,
    JSON.stringify({
      ...data,
      fetchedAt: Date.now(),
    }),
  )
}

export const getLatestEurRates = async ({
  forceRefresh = false,
} = {}) => {
  const cachedData = readCache()

  if (!forceRefresh && cacheIsValid(cachedData)) {
    return cachedData
  }

  try {
    const response = await fetch(
      `${API_URL}/rates?base=EUR`,
    )

    if (!response.ok) {
      throw new Error(
        `Exchange-rate request failed with status ${response.status}`,
      )
    }

    const apiRates = await response.json()

    const rates = {
      EUR: 1,
    }

    apiRates.forEach((item) => {
      if (
        item.quote &&
        Number.isFinite(Number(item.rate)) &&
        Number(item.rate) > 0
      ) {
        rates[item.quote] = Number(item.rate)
      }
    })

    const result = {
      base: 'EUR',
      date: apiRates[0]?.date ?? null,
      rates,
    }

    saveCache(result)

    return {
      ...result,
      fetchedAt: Date.now(),
    }
  } catch (error) {
    // Si la API falla, usamos incluso una caché caducada
    if (cachedData?.rates) {
      return {
        ...cachedData,
        stale: true,
      }
    }

    throw error
  }
}

// Frankfurter returns 1 EUR = X units of the other currency
export const convertMoneyToEur = (
  estimatedValue,
  rates,
) => {
  if (estimatedValue === null || estimatedValue === undefined) {
    return null
  }

  // temp compatibility
  if (typeof estimatedValue === 'number') {
    return Number.isFinite(estimatedValue)
      ? estimatedValue
      : null
  }

  const amount = Number(estimatedValue.amount)
  const currency = estimatedValue.currency?.toUpperCase()

  if (!Number.isFinite(amount) || !currency) {
    return null
  }

  if (currency === 'EUR') {
    return amount
  }

  const exchangeRate = rates?.[currency]

  if (
    !Number.isFinite(exchangeRate) ||
    exchangeRate <= 0
  ) {
    return null
  }

  return amount / exchangeRate
}