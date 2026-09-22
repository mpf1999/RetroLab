import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import { Link } from 'react-router-dom'
import {
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
} from 'recharts'
import {
  getRepairCases,
} from '../api/repairCaseApi'

import {
  getConsoles,
} from '../api/consoleApi'

import getErrorMessage from '../utils/getErrorMessage'

import {
  convertMoneyToEur,
  getLatestEurRates,
} from '../services/exchangeRateService'
const STATUS_CONFIG = {
  OPEN: {
    label: 'Open',
    color: '#f97316',
    badge:
      'bg-orange-100 text-orange-700',
  },

  IN_PROGRESS: {
    label: 'In progress',
    color: '#2563eb',
    badge:
      'bg-blue-100 text-blue-700',
  },

  CLOSED: {
    label: 'Closed',
    color: '#16a34a',
    badge:
      'bg-green-100 text-green-700',
  },
}

const Dashboard = () => {
  const [
    repairCases,
    setRepairCases,
  ] = useState([])

  const [loading, setLoading] =
    useState(true)

  const [error, setError] =
    useState('')

  const [
    exchangeRates,
    setExchangeRates,
  ] = useState({
    EUR: 1,
  })

  const [
    ratesLoading,
    setRatesLoading,
  ] = useState(true)

  const [
    ratesError,
    setRatesError,
  ] = useState(false)

  // load concurrently

  const loadDashboardData =
    async () => {
      try {
        setLoading(true)
        setError('')

        const [
          repairCasesData,
          consolesData,
        ] = await Promise.all([
          getRepairCases(),
          getConsoles(),
        ])

        const cases = Array.isArray(
          repairCasesData,
        )
          ? repairCasesData
          : []

        const consoles = Array.isArray(
          consolesData,
        )
          ? consolesData
          : []
        
        const casesWithConsoleData =
          cases.map((repairCase) => {
            const consoleData =
              consoles.find(
                (consoleItem) => {
                  const consoleId =
                    consoleItem.consoleId ??
                    consoleItem.id

                  return (
                    String(consoleId) ===
                    String(
                      repairCase.consoleId,
                    )
                  )
                },
              )
            return {
              ...repairCase,

              consoleModelName:
                repairCase
                  .consoleModelName ??
                consoleData
                  ?.consoleModelName ??
                null,

              manufacturerName:
                repairCase
                  .manufacturerName ??
                consoleData
                  ?.manufacturerName ??
                null,

              serialNumber:
                consoleData
                  ?.serialNumber ??
                null,

              estimatedValue:
                normalizeEstimatedValue(
                  consoleData,
                ),
            }
          })

        setRepairCases(
          casesWithConsoleData,
        )
      } catch (requestError) {
        setRepairCases([])

        setError(
          getErrorMessage(
            requestError,
            'Could not load dashboard data.',
          ),
        )
      } finally {
        setLoading(false)
      }
    }

  // load when mounted

  useEffect(() => {
    loadDashboardData()
  }, [])

  // load exchange rate differently
  useEffect(() => {
    let componentMounted = true

    const loadExchangeRates =
      async () => {
        try {
          setRatesLoading(true)
          const result =
            await getLatestEurRates()

          if (!componentMounted) {
            return
          }

          setExchangeRates({
            EUR: 1,
            ...(result.rates ?? {}),
          })

          setRatesError(false)
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

  // group repair cases by status once, not to filter over and over again
  const repairCasesByStatus =
    useMemo(() => {
      const groupedCases = {
        OPEN: [],
        IN_PROGRESS: [],
        CLOSED: [],
      }

      repairCases.forEach(
        (repairCase) => {
          if (
            groupedCases[
              repairCase.status
            ]
          ) {
            groupedCases[
              repairCase.status
            ].push(repairCase)
          }
        },
      )

      return groupedCases
    }, [repairCases])

  // transform into rechart structure
  const casesByStatus =
    useMemo(() => {
      return Object.entries(
        STATUS_CONFIG,
      ).map(
        ([status, config]) => ({
          status,
          name: config.label,
          value:
            repairCasesByStatus[
              status
            ]?.length ?? 0,
          color: config.color,
        }),
      )
    }, [repairCasesByStatus])
  const openRepairCases =
    repairCasesByStatus.OPEN

  const closedRepairCases =
    repairCasesByStatus.CLOSED

  /*
   * Calculate the percentage of repair cases that are closed.
   */
  const closedPercentage =
    repairCases.length === 0
      ? 0
      : Math.round(
          (closedRepairCases.length /
            repairCases.length) *
            100,
        )
  const closedEstimatedValueInEur =
    useMemo(() => {
      return closedRepairCases.reduce(
        (
          total,
          repairCase,
        ) => {
          if (
            !repairCase.estimatedValue
          ) {
            return total
          }

          const valueInEur =
            convertMoneyToEur(
              repairCase
                .estimatedValue,
              exchangeRates,
            )

          return valueInEur === null
            ? total
            : total + valueInEur
        },
        0,
      )
    }, [
      closedRepairCases,
      exchangeRates,
    ])

  // count closed repair cases
  const closedCasesWithValue =
    useMemo(() => {
      return closedRepairCases.filter(
        (repairCase) =>
          repairCase.estimatedValue,
      ).length
    }, [closedRepairCases])
  const formatCurrency = (
    amount,
  ) => {
    return new Intl.NumberFormat(
      undefined,
      {
        style: 'currency',
        currency: 'EUR',
        maximumFractionDigits: 2,
      },
    ).format(amount)
  }

  // render loading state until request finished

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center gap-3 text-slate-500">

        <span>
          Loading dashboard...
        </span>
      </div>
    )
  }

  // error
  if (error) {
    return (
      <section>
        <DashboardHeader />

        <div
          role="alert"
          className="mt-6 flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
        >
          <div>
            <p>{error}</p>

            <button
              type="button"
              onClick={
                loadDashboardData
              }
              className="mt-3 flex items-center gap-2 font-semibold"
            >

              <span>
                Try again
              </span>
            </button>
          </div>
        </div>
      </section>
    )
  }

  return (
    <section>
      <DashboardHeader />

      <div className="mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          title="Total repair cases"
          value={
            repairCases.length
          }
        />
        <StatCard
          title="Open cases"
          value={
            openRepairCases.length
          }
        />
        <StatCard
          title="Closed cases"
          value={`${closedPercentage}%`}
          subtitle={`${closedRepairCases.length} of ${repairCases.length} cases`}
        />
        <StatCard
          title="Closed estimated value"
          value={
            ratesLoading
              ? 'Loading...'
              : formatCurrency(
                  closedEstimatedValueInEur,
                )
          }
          subtitle={
            getEstimatedValueSubtitle({
              ratesError,
              ratesLoading,
              closedCases:
                closedRepairCases.length,
              casesWithValue:
                closedCasesWithValue,
            })
          }
        />
      </div>

      <div className="mt-6 grid items-start gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(320px,1fr)]">
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200 px-6 py-5">
            <div>
              <h2 className="text-lg font-semibold text-slate-900">
                Open repair cases
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Repair cases waiting to be processed.
              </p>
            </div>

            <Link
              to="/repair-cases"
              className="flex items-center gap-1 text-sm font-semibold text-primary-600 hover:text-primary-700"
            >
              <span>View all</span>
            </Link>
          </div>

          {openRepairCases.length >
          0 ? (
            <div className="max-h-128 divide-y divide-slate-100 overflow-y-auto">
              {openRepairCases.map(
                (repairCase) => (
                  <OpenRepairCase
                    key={
                      repairCase
                        .repairCaseId ??
                      repairCase.id
                    }
                    repairCase={
                      repairCase
                    }
                  />
                ),
              )}
            </div>
          ) : (
            <div className="p-12 text-center">

              <h3 className="mt-4 font-semibold text-slate-800">
                No open repair cases
              </h3>

              <p className="mt-2 text-sm text-slate-500">
                There are currently no open repair cases.
              </p>
            </div>
          )}
        </div>

        {/* Status distribution represented as a donut chart. */}
        <div className="self-start rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <div>
            <h2 className="text-lg font-semibold text-slate-900">
              Repair cases by status
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Current distribution of repair cases.
            </p>
          </div>

          {repairCases.length > 0 ? (
            <div className="mt-4 h-72">
              <ResponsiveContainer
                width="100%"
                height="100%"
              >
                <PieChart>
                  <Pie
                    data={
                      casesByStatus
                    }
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="45%"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={3}
                  >
                    {casesByStatus.map(
                      (entry) => (
                        <Cell
                          key={
                            entry.status
                          }
                          fill={
                            entry.color
                          }
                        />
                      ),
                    )}
                  </Pie>

                  <Tooltip
                    formatter={(
                      value,
                      name,
                    ) => [
                      `${value} ${
                        value === 1
                          ? 'case'
                          : 'cases'
                      }`,
                      name,
                    ]}
                  />

                  <Legend
                    verticalAlign="bottom"
                    height={36}
                  />
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex h-72 flex-col items-center justify-center text-slate-400">

              <p className="mt-3 text-sm">
                No repair case data available.
              </p>
            </div>
          )}
        </div>
      </div>
    </section>
  )
}

// static heading kept separate

const DashboardHeader = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold text-slate-900">
        Dashboard
      </h1>
      <p className="mt-2 text-slate-500">
        Overview of your repair activity.
      </p>
    </div>
  )
}

// reusable text only card
const StatCard = ({
  title,
  value,
  subtitle,
}) => {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="text-sm font-medium text-slate-500">
            {title}
          </p>

          <p className="mt-2 truncate text-2xl font-bold text-slate-900">
            {value}
          </p>

          {subtitle && (
            <p className="mt-1 text-xs text-slate-400">
              {subtitle}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}

const OpenRepairCase = ({
  repairCase,
}) => {
  const statusConfig =
    STATUS_CONFIG[
      repairCase.status
    ] ?? {
      label:
        repairCase.status ??
        'Unknown',
      badge:
        'bg-slate-100 text-slate-700',
    }

  const repairCaseId =
    repairCase.repairCaseId ??
    repairCase.id

  const formattedDate =
    formatDate(
      repairCase.startDate,
    )

  const consoleName = [
    repairCase.manufacturerName,
    repairCase.consoleModelName,
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <Link
      to={`/repair-cases/${repairCaseId}`}
      className="flex flex-col gap-4 px-6 py-5 transition-colors hover:bg-slate-50 sm:flex-row sm:items-center sm:justify-between"
    >
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <h3 className="truncate font-semibold text-slate-900">
            {repairCase.title ||
              `Repair case ${repairCaseId}`}
          </h3>
        </div>

        {consoleName && (
          <p className="mt-1 text-sm text-slate-500">
            {consoleName}
          </p>
        )}

        {repairCase.serialNumber && (
          <p className="mt-1 font-mono text-xs text-slate-400">
            {
              repairCase.serialNumber
            }
          </p>
        )}
      </div>

      <div className="shrink-0 text-sm text-slate-500">
        Opened {formattedDate}
      </div>
    </Link>
  )
}

// normalize into MoneyDTO like object
const normalizeEstimatedValue = (
  consoleData,
) => {
  if (!consoleData) {
    return null
  }

  if (
    consoleData.estimatedValue &&
    typeof consoleData
      .estimatedValue === 'object'
  ) {
    const amount = Number(
      consoleData
        .estimatedValue.amount,
    )

    const currency =
      consoleData
        .estimatedValue.currency ??
      'EUR'

    if (!Number.isFinite(amount)) {
      return null
    }

    return {
      amount,
      currency,
    }
  }
  if (
    consoleData.estimatedValue !==
      null &&
    consoleData.estimatedValue !==
      undefined &&
    typeof consoleData
      .estimatedValue !== 'object'
  ) {
    const amount = Number(
      consoleData.estimatedValue,
    )

    if (!Number.isFinite(amount)) {
      return null
    }
    return {
      amount,

      currency:
        consoleData.currency ??
        consoleData
          .estimatedValueCurrency ??
        'EUR',
    }
  }

  return null
}

const getEstimatedValueSubtitle = ({
  ratesError,
  ratesLoading,
  closedCases,
  casesWithValue,
}) => {
  if (ratesLoading) {
    return 'Loading exchange rates'
  }

  if (ratesError) {
    return 'Some currencies could not be converted'
  }

  if (closedCases === 0) {
    return 'No closed repair cases'
  }

  if (casesWithValue === 0) {
    return 'No estimated values available'
  }

  return `${casesWithValue} ${
    casesWithValue === 1
      ? 'console'
      : 'consoles'
  } normalized to EUR`
}

const formatDate = (date) => {
  if (!date) {
    return 'No date'
  }

  const parsedDate = new Date(
    date,
  )

  if (
    Number.isNaN(
      parsedDate.getTime(),
    )
  ) {
    return 'No date'
  }

  return new Intl.DateTimeFormat(
    undefined,
    {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    },
  ).format(parsedDate)
}

export default Dashboard