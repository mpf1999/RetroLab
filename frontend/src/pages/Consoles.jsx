import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  Link,
} from 'react-router-dom'

import {
  AlertCircle,
  Archive,
  ArrowUpDown,
  Gamepad2,
  Hash,
  LoaderCircle,
  Plus,
} from 'lucide-react'

import {
  getConsoles,
} from '../api/consoleApi'

import {
  convertMoneyToEur,
  getLatestEurRates,
} from '../services/exchangeRateService'

import {
  resolveImageUrl,
} from '../utils/imageUrl'
import SearchField from '../components/SearchField'
import Pagination from '../components/Pagination'

const SORT_OPTIONS = {
  NAME_ASC: 'NAME_ASC',
  NAME_DESC: 'NAME_DESC',

  MANUFACTURER_ASC:
    'MANUFACTURER_ASC',

  MANUFACTURER_DESC:
    'MANUFACTURER_DESC',

  MODEL_ASC: 'MODEL_ASC',
  MODEL_DESC: 'MODEL_DESC',

  PRICE_ASC: 'PRICE_ASC',
  PRICE_DESC: 'PRICE_DESC',
}

const Consoles = () => {
  const [consoles, setConsoles] =
    useState([])

  const [query, setQuery] =
    useState('')

  const [
    exchangeRates,
    setExchangeRates,
  ] = useState({
    EUR: 1,
  })

  const [
    itemsPerPage,
    setItemsPerPage,
  ] = useState(10)

  const [
    currentPage,
    setCurrentPage,
  ] = useState(1)

  const [
    sortOption,
    setSortOption,
  ] = useState(
    SORT_OPTIONS.NAME_ASC,
  )

  const [loading, setLoading] =
    useState(true)

  const [
    ratesLoading,
    setRatesLoading,
  ] = useState(true)

  const [error, setError] =
    useState('')

  const [
    ratesError,
    setRatesError,
  ] = useState(false)

  // load consoles when page is mounted
  useEffect(() => {
    let componentMounted = true

    const loadConsoles =
      async () => {
        try {
          setLoading(true)
          setError('')

          const data =
            await getConsoles()
          if (!componentMounted) {
            return
          }

          setConsoles(
            Array.isArray(data)
              ? data
              : [],
          )
        } catch (requestError) {
          if (!componentMounted) {
            return
          }

          setConsoles([])
          setError(
            requestError.userMessage ??
              requestError.message ??
              'Could not load consoles.',
          )
        } finally {
          if (componentMounted) {
            setLoading(false)
          }
        }
      }
    loadConsoles()
    return () => {
      componentMounted = false
    }
  }, [])

  // load latest exchange rates, EUR is the base currency
  useEffect(() => {
    let componentMounted = true

    const loadExchangeRates =
      async () => {
        try {
          setRatesLoading(true)
          setRatesError(false)

          const result =
            await getLatestEurRates()

          if (!componentMounted) {
            return
          }
          setExchangeRates({
            EUR: 1,
            ...(result.rates ?? {}),
          })
        } catch (requestError) {
          console.error(
            'Could not load exchange rates:',
            requestError,
          )

          if (componentMounted) {
            setRatesError(true)
          }
        } finally {
          if (componentMounted) {
            setRatesLoading(false)
          }
        }
      }

    loadExchangeRates()

    return () => {
      componentMounted = false
    }
  }, [])

  // archived are excluded
  const activeConsoles =
    useMemo(() => {
      const normalizedQuery =
        query.trim().toLowerCase()

      return consoles.filter(
        (consoleItem) => {
          if (
            consoleItem.status ===
            'ARCHIVED'
          ) {
            return false
          }

          if (!normalizedQuery) {
            return true
          }
          const searchableText = [
            consoleItem.manufacturerName,
            consoleItem.consoleModelName,
            consoleItem.serialNumber,
            consoleItem.region,
            consoleItem.color,
            consoleItem.status,
          ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()

          return searchableText.includes(
            normalizedQuery,
          )
        },
      )
    }, [consoles, query])

  // sort only active consoles, price sorting converts internally to EUR
  const sortedConsoles =
    useMemo(() => {
      return sortConsoles(
        activeConsoles,
        sortOption,
        exchangeRates,
      )
    }, [
      activeConsoles,
      sortOption,
      exchangeRates,
    ])

  // development diagnostic to inspect currency conversion in the browser console

  useEffect(() => {
    if (
    ratesLoading ||
      ratesError
    ) {
      return
    }

    console.table(
      consoles.map(
        (consoleItem) => {
          const currency =
            consoleItem
              .estimatedValue
              ?.currency
              ?.toUpperCase()

          return {console:
              consoleItem
                .consoleModelName,

            originalAmount:
              consoleItem
                .estimatedValue
                ?.amount,

            currency,

            exchangeRate:
              exchangeRates[
                currency
              ],

            valueInEur:
              getMoneyAmountInEur(
                consoleItem,
                exchangeRates,
              ),
          }
        },
      ),
    )
  }, [
    consoles,
    exchangeRates,
    ratesLoading,
    ratesError,
  ])

  // calculate which consoles belong to the current page after filtering and sorting
  const totalPages = Math.max(
    1,
    Math.ceil(
      sortedConsoles.length /
        itemsPerPage,
    ),
  )

  const safeCurrentPage =
    Math.min(
      currentPage,
      totalPages,
    )

  const firstConsoleIndex =
    (safeCurrentPage - 1) *
    itemsPerPage

  const lastConsoleIndex =
    firstConsoleIndex +
    itemsPerPage

  const visibleConsoles =
    sortedConsoles.slice(
      firstConsoleIndex,
      lastConsoleIndex,
    )
  const firstVisibleItem =
    sortedConsoles.length === 0
      ? 0
      : firstConsoleIndex + 1

  const lastVisibleItem =
    Math.min(
      lastConsoleIndex,
      sortedConsoles.length,
    )

  // changing page size or sort order resets pagination

  const handleItemsPerPageChange = (
    event,
  ) => {
    setItemsPerPage(
      Number(event.target.value),
    )
    setCurrentPage(1)
  }

  const handleSortChange = (
    event,
  ) => {
    setSortOption(
      event.target.value,
    )

    setCurrentPage(1)
  }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center">
        <LoaderCircle className="h-9 w-9 animate-spin text-primary-500" />
      </div>
    )
  }

  return (
    <section>
      {/* Page header and inventory actions. */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">
            Consoles
          </h1>

          <p className="mt-2 text-slate-500">
            View and manage the
            consoles registered in
            RetroLab.
          </p>
        </div>

        <div className="flex flex-col gap-3 sm:flex-row">
          <Link
            to="/consoles/archived"
            className="flex items-center justify-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-3 font-semibold text-slate-700 transition-colors hover:bg-slate-100"
          >
            <Archive className="h-5 w-5" />

            <span>
              Archived consoles
            </span>
          </Link>

          <Link
            to="/consoles/new"
            className="flex items-center justify-center gap-2 rounded-lg bg-primary-500 px-4 py-3 font-semibold text-white transition-colors hover:bg-primary-600"
          >
            <Plus className="h-5 w-5" />

            <span>
              Add console
            </span>
          </Link>
        </div>
      </div>

      {error && (
        <div className="mt-6 flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />

          <p>{error}</p>
        </div>
      )}
      <div className="mt-8 grid min-w-0 gap-4 rounded-xl border border-slate-200 bg-white p-4 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
        <div className="w-full sm:w-80 lg:w-96">
          <SearchField
                    id="console-search"
                    value={query}
                    onChange={(value) => {
                      setQuery(value)
                      setCurrentPage(1)
                    }}
                    placeholder="Model, manufacturer or serial..."
                  />
        </div>

        <div className="flex min-w-0 flex-col gap-3 sm:flex-row sm:items-end lg:justify-end">
          <div className="min-w-0 sm:w-56">
            <label
              htmlFor="console-sort"
              className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
            >
              Sort by
            </label>

            <div className="relative">
              <ArrowUpDown className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />

              <select
                id="console-sort"
                value={sortOption}
                onChange={handleSortChange}
                className="w-full min-w-0 rounded-lg border border-slate-300 bg-white py-2 pl-9 pr-8 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
              >
                <option value={SORT_OPTIONS.NAME_ASC}>
                  Name A–Z
                </option>
                <option value={SORT_OPTIONS.NAME_DESC}>
                  Name Z–A
                </option>
                <option value={SORT_OPTIONS.MANUFACTURER_ASC}>
                  Manufacturer A–Z
                </option>
                <option value={SORT_OPTIONS.MANUFACTURER_DESC}>
                  Manufacturer Z–A
                </option>
                <option value={SORT_OPTIONS.MODEL_ASC}>
                  Console model A–Z
                </option>
                <option value={SORT_OPTIONS.MODEL_DESC}>
                  Console model Z–A
                </option>
                <option
                  value={SORT_OPTIONS.PRICE_ASC}
                  disabled={ratesLoading || ratesError}
                >
                  Estimated value: lowest
                </option>
                <option
                  value={SORT_OPTIONS.PRICE_DESC}
                  disabled={ratesLoading || ratesError}
                >
                  Estimated value: highest
                </option>
              </select>
            </div>
          </div>

          <div>
            <label
              htmlFor="items-per-page"
              className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
            >
              Items per page
            </label>

            <select
              id="items-per-page"
              value={itemsPerPage}
              onChange={handleItemsPerPageChange}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>
      {ratesError && (
        <div className="mt-3 flex items-start gap-3 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-700">
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />

          <p>
            Price sorting is unavailable because exchange rates could not be loaded.
          </p>
        </div>
      )}
      {visibleConsoles.length > 0 ? (
        <div className="mt-6 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          {visibleConsoles.map(
            (consoleItem) => (
              <ConsoleCard
                key={
                  consoleItem.consoleId
                }
                consoleItem={
                  consoleItem
                }
              />
            ),
          )}
        </div>
      ) : (
        <div className="mt-6 rounded-xl border border-slate-200 bg-white p-12 text-center">
          <Gamepad2 className="mx-auto h-12 w-12 text-slate-300" />

          <h2 className="mt-4 text-lg font-semibold text-slate-800">
            No consoles found
          </h2>

          <p className="mt-2 text-sm text-slate-500">
            Add a console to start managing your inventory.
          </p>
        </div>
      )}

      <Pagination
        currentPage={safeCurrentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </section>
  )
}

// reusable card, image failures handled locally
const ConsoleCard = ({
  consoleItem,
}) => {
  const [
    imageError,
    setImageError,
  ] = useState(false)

  const imageUrl =
    resolveImageUrl(
      consoleItem.imageUrl,
    )

  const hasImage =
    Boolean(imageUrl) &&
    !imageError

  return (
    <Link
      to={`/consoles/${consoleItem.consoleId}`}
      className="group overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition-all hover:-translate-y-1 hover:border-primary-300 hover:shadow-md"
    >
      <div className="flex aspect-video items-center justify-center overflow-hidden bg-slate-100">
        {hasImage ? (
          <img
            src={imageUrl}
            alt={`${consoleItem.manufacturerName} ${consoleItem.consoleModelName}`}
            onError={() =>
              setImageError(true)
            }
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="flex flex-col items-center gap-3 text-slate-400">
            <Gamepad2 className="h-14 w-14" />

            <span className="text-sm">
              No image available
            </span>
          </div>
        )}
      </div>

      <div className="p-5">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-sm font-medium text-primary-600">
              {
                consoleItem
                  .manufacturerName
              }
            </p>

            <h2 className="mt-1 text-xl font-semibold text-slate-900">
              {
                consoleItem
                  .consoleModelName
              }
            </h2>
          </div>

          <StatusBadge
            status={
              consoleItem.status
            }
          />
        </div>

        <div className="mt-4 flex items-center gap-2 text-sm text-slate-500">
          <Hash className="h-4 w-4 shrink-0" />

          <span className="truncate font-mono">
            {
              consoleItem
                .serialNumber
            }
          </span>
        </div>

        <p className="mt-4 font-semibold text-slate-800">
          {formatMoney(
            consoleItem
              .estimatedValue,
          )}
        </p>
      </div>
    </Link>
  )
}

// translate console status intu visual style
const StatusBadge = ({
  status,
}) => {
  const styles = {
    AVAILABLE:
      'bg-blue-100 text-blue-700',

    IN_REPAIR:
      'bg-amber-100 text-amber-700',

    REPAIRED:
      'bg-emerald-100 text-emerald-700',

    RETURNED:
      'bg-purple-100 text-purple-700',

    SOLD:
      'bg-cyan-100 text-cyan-700',

    ARCHIVED:
      'bg-slate-100 text-slate-600',
  }

  return (
    <span
      className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold ${
        styles[status] ??
        'bg-slate-100 text-slate-600'
      }`}
    >
      {formatEnum(status)}
    </span>
  )
}

// return a sorted copy of the console list, original array is never mutated
const sortConsoles = (
  consoles,
  sortOption,
  exchangeRates,
) => {
  return [...consoles].sort(
    (
      firstConsole,
      secondConsole,
    ) => {
      switch (sortOption) {
        case SORT_OPTIONS.NAME_ASC:
          return compareText(
            getConsoleName(
              firstConsole,
            ),
            getConsoleName(
              secondConsole,
            ),
          )
        case SORT_OPTIONS.NAME_DESC:
          return compareText(
            getConsoleName(
              secondConsole,
            ),
            getConsoleName(
              firstConsole,
            ),
          )
        case SORT_OPTIONS.MANUFACTURER_ASC:
          return (
            compareText(
              firstConsole
                .manufacturerName,
              secondConsole
                .manufacturerName,
            ) ||
            compareText(
              firstConsole
                .consoleModelName,
              secondConsole
                .consoleModelName,
            )
          )
        case SORT_OPTIONS.MANUFACTURER_DESC:
          return (
            compareText(
              secondConsole
                .manufacturerName,
              firstConsole
                .manufacturerName,
            ) ||
            compareText(
              firstConsole
                .consoleModelName,
              secondConsole
                .consoleModelName,
            )
          )

        case SORT_OPTIONS.MODEL_ASC:
          return compareText(
            firstConsole
              .consoleModelName,
            secondConsole
              .consoleModelName,
          )
        case SORT_OPTIONS.MODEL_DESC:
          return compareText(
            secondConsole
              .consoleModelName,
            firstConsole
              .consoleModelName,
          )
        case SORT_OPTIONS.PRICE_ASC:
          return comparePriceInEur(
            firstConsole,
            secondConsole,
            exchangeRates,
            'ASC',
          )

        case SORT_OPTIONS.PRICE_DESC:
          return comparePriceInEur(
            firstConsole,
            secondConsole,
            exchangeRates,
            'DESC',
          )

        default:
          return 0
      }
    },
  )
}

const compareText = (
  firstValue,
  secondValue,
) => {
  return String(
    firstValue ?? '',
  ).localeCompare(
    String(
      secondValue ?? '',
    ),
    undefined,
    {
      sensitivity: 'base',
      numeric: true,
    },
  )
}

const getConsoleName = (
  consoleItem,
) => {
  return [
    consoleItem.manufacturerName,
    consoleItem.consoleModelName,
  ]
    .filter(Boolean)
    .join(' ')
}

const getMoneyAmountInEur = (
  consoleItem,
  exchangeRates,
) => {
  const convertedAmount =
    convertMoneyToEur(
      consoleItem.estimatedValue,
      exchangeRates,
    )

  return Number.isFinite(
    convertedAmount,
  )
    ? convertedAmount
    : null
}

const comparePriceInEur = (
  firstConsole,
  secondConsole,
  exchangeRates,
  direction,
) => {
  const firstPrice =
    getMoneyAmountInEur(
      firstConsole,
      exchangeRates,
    )

  const secondPrice =
    getMoneyAmountInEur(
      secondConsole,
      exchangeRates,
    )

  if (
    firstPrice === null &&
    secondPrice === null
  ) {
    return 0
  }
  if (firstPrice === null) {
    return 1
  }
  if (secondPrice === null) {
    return -1
  }
  if (direction === 'ASC') {
    return (
      firstPrice - secondPrice
    )
  }

  return (
    secondPrice - firstPrice
  )
}

// format MoneyDTIO through broser locale and ISO, text as a fallback
const formatMoney = (money) => {
  if (
    !money ||
    money.amount === null ||
    money.amount === undefined ||
    !money.currency
  ) {
    return '—'
  }

  try {
    return new Intl.NumberFormat(
      undefined,
      {
        style: 'currency',
        currency:
          money.currency,
      },
    ).format(
      Number(money.amount),
    )
  } catch {
    return `${money.amount} ${money.currency}`
  }
}

const formatEnum = (value) => {
  if (!value) {
    return '—'
  }

  return value
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(
      /\b\w/g,
      (character) =>
        character.toUpperCase(),
    )
}


export default Consoles
