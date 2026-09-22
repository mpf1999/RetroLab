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
  LoaderCircle,
  Plus,
  Wrench,
} from 'lucide-react'

import {
  getRepairCases,
} from '../api/repairCaseApi'
import SearchField from '../components/SearchField'
import Pagination from '../components/Pagination'
import getErrorMessage from '../utils/getErrorMessage'


const STATUS_OPTIONS = [
  {
    value: 'ALL',
    label: 'All statuses',
  },
  {
    value: 'OPEN',
    label: 'Open',
  },
  {
    value: 'IN_PROGRESS',
    label: 'In progress',
  },
  {
    value: 'CLOSED',
    label: 'Closed',
  },
]


const RepairCases = () => {

  // repair cases from backend
  const [
    repairCases,
    setRepairCases,
  ] = useState([])


  // status selected by user, ALL disables filtering
  const [
    statusFilter,
    setStatusFilter,
  ] = useState('ALL')

  const [
    query,
    setQuery,
  ] = useState('')
  const [
    itemsPerPage,
    setItemsPerPage,
  ] = useState(10)

  const [
    currentPage,
    setCurrentPage,
  ] = useState(1)

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    error,
    setError,
  ] = useState('')


  // retrieve repair case collection when the page is mounted

  useEffect(() => {

    const loadRepairCases = async () => {

      try {

        setLoading(true)
        setError('')


        const data =
          await getRepairCases()
        setRepairCases(
          Array.isArray(data)
            ? data
            : [],
        )

      } catch (requestError) {

        setError(
          getErrorMessage(
            requestError,
            'Could not load repair cases.',
          ),
        )

      } finally {

        setLoading(false)
      }
    }


    loadRepairCases()

  }, [])

  const filteredRepairCases =
    useMemo(() => {

      const normalizedQuery =
        query.trim().toLowerCase()


      return repairCases.filter(
        (repairCase) => {

          const matchesStatus =
            statusFilter === 'ALL' ||
            repairCase.status ===
              statusFilter


          // releveant fields into one normalized searchable string
          const searchableText = [
            repairCase.title,
            repairCase.description,
            repairCase.consoleName,
            repairCase.status,
          ]
            .filter(Boolean)
            .join(' ')
            .toLowerCase()


          return (
            matchesStatus &&
            searchableText.includes(
              normalizedQuery,
            )
          )
        },
      )

    }, [
      repairCases,
      query,
      statusFilter,
    ])

  const totalPages = Math.max(
    1,
    Math.ceil(
      filteredRepairCases.length /
        itemsPerPage,
    ),
  )

  const safePage = Math.min(
    currentPage,
    totalPages,
  )

  const firstIndex =
    (safePage - 1) *
    itemsPerPage


  // get only current page with slice
  const visibleRepairCases =
    filteredRepairCases.slice(
      firstIndex,
      firstIndex + itemsPerPage,
    )

  const changeFilters = (
    callback,
  ) => {

    callback()

    setCurrentPage(1)
  }

  if (loading) {

    return (
      <div className="flex min-h-80 items-center justify-center">

        <LoaderCircle className="h-8 w-8 animate-spin text-primary-500" />

      </div>
    )
  }


  return (

    <section>

      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">


        <div>

          <h1 className="text-3xl font-bold text-slate-900">
            Repair Cases
          </h1>


          <p className="mt-2 text-slate-500">
            View and manage console repair cases.
          </p>

        </div>
        <div className="flex flex-col gap-3 sm:flex-row">


          <Link
            to="/repair-cases/new"
            className="flex items-center justify-center gap-2 rounded-lg bg-primary-500 px-4 py-3 font-semibold text-white transition-colors hover:bg-primary-600"
          >

            <Plus className="h-5 w-5" />
            New Repair Case

          </Link>
        </div>

      </div>

      {error && (

        <div className="mt-6 flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">

          <AlertCircle className="h-5 w-5 shrink-0" />

          {error}

        </div>

      )}

      <div className="mt-8 grid gap-4 rounded-xl border border-slate-200 bg-white p-4 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
        <div className="w-full sm:w-80 lg:w-96">
          <SearchField
            id="repair-case-search"
            value={query}
            onChange={(value) =>
              changeFilters(() =>
                setQuery(value),
              )
            }
            placeholder="Search repair cases..."
          />
        </div>

        <div className="flex flex-col gap-3 sm:flex-row sm:items-end lg:justify-end">
          <div>
            <label htmlFor="repair-status-filter" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Status
            </label>
            <select
              id="repair-status-filter"
              value={statusFilter}
              onChange={(event) =>
                changeFilters(() =>
                  setStatusFilter(event.target.value),
                )
              }
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
            >
              {STATUS_OPTIONS.map((status) => (
                <option key={status.value} value={status.value}>
                  {status.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="repair-cases-per-page" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Items per page
            </label>
            <select
              id="repair-cases-per-page"
              value={itemsPerPage}
              onChange={(event) =>
                changeFilters(() =>
                  setItemsPerPage(Number(event.target.value)),
                )
              }
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {visibleRepairCases.length === 0 ? (

        <div className="mt-6 rounded-xl border border-slate-200 bg-white p-12 text-center">

          <Wrench className="mx-auto h-12 w-12 text-slate-300" />


          <h2 className="mt-4 text-lg font-semibold text-slate-800">
            No repair cases found
          </h2>

        </div>

      ) : (

        <div className="mt-6 grid gap-5 md:grid-cols-2 xl:grid-cols-3">


          {visibleRepairCases.map(
            (repairCase) => (

              <Link
                key={
                  repairCase.repairCaseId
                }

                to={
                  `/repair-cases/${repairCase.repairCaseId}`
                }

                className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
              >


                <div className="flex items-start justify-between gap-3">


                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-amber-100 text-amber-600">

                    <Wrench className="h-5 w-5" />

                  </div>


                  <StatusBadge
                    status={
                      repairCase.status
                    }
                  />

                </div>


                <h2 className="mt-4 text-lg font-semibold text-slate-900">

                  {repairCase.title}

                </h2>


                <p className="mt-1 text-sm font-medium text-primary-600">

                  {repairCase.consoleName}

                </p>


                <p className="mt-3 line-clamp-2 text-sm text-slate-500">

                  {repairCase.description}

                </p>


                <p className="mt-4 text-xs text-slate-400">

                  Started{' '}

                  {formatDate(
                    repairCase.startDate,
                  )}

                </p>

              </Link>

            ),
          )}

        </div>

      )}
      <Pagination
        currentPage={safePage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />

    </section>
  )
}

const StatusBadge = ({
  status,
}) => {

  const styles = {

    OPEN:
      'bg-blue-100 text-blue-700',

    IN_PROGRESS:
      'bg-amber-100 text-amber-700',

    CLOSED:
      'bg-emerald-100 text-emerald-700',

  }


  return (

    <span
      className={`rounded-full px-3 py-1 text-xs font-semibold ${
        styles[status] ??
        'bg-slate-100 text-slate-600'
      }`}
    >

      {status?.replaceAll(
        '_',
        ' ',
      )}
    </span>
  )
}

const formatDate = (
  date,
) =>
  date
    ? new Intl.DateTimeFormat(
        'en-GB',
        {
          dateStyle: 'medium',
        },
      ).format(
        new Date(date),
      )
    : '—'


export default RepairCases