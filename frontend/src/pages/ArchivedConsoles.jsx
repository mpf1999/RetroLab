import {
  useCallback,
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  useNavigate,
} from 'react-router-dom'

import {
  Archive,
  ArrowLeft,
  Gamepad2,
  LoaderCircle,
  RefreshCw,
  Search,
} from 'lucide-react'

import {
  getConsoles,
} from '../api/consoleApi'


const ArchivedConsoles = () => {

  // useNavigate to navigate programmatically to other route
  const navigate = useNavigate()


  // store all consoles from backend
  const [
    consoles,
    setConsoles,
  ] = useState([])


  // stores text in search field
  const [
    search,
    setSearch,
  ] = useState('')


  // indicates whether consoles are currently being loaded
  const [
    loading,
    setLoading,
  ] = useState(true)
  const [
    error,
    setError,
  ] = useState('')


  // load all consoles from backend, useCallback keeps the same function reference between renders
  const loadConsoles = useCallback(
    async () => {
      try {
        setLoading(true)
        setError('')
        const consolesData =
          await getConsoles()

        setConsoles(
          Array.isArray(consolesData)
            ? consolesData
            : [],
        )

      } catch (requestError) {

        setConsoles([])


        // try to get error, if not generic
        setError(
          requestError.userMessage ??
            requestError.response?.data
              ?.message ??
            requestError.message ??
            'Could not load archived consoles.',
        )

      } finally {
        setLoading(false)
      }
    },
    [],
  )


  // load consoles when the component is mounted, reference stable because of useCallback
  useEffect(() => {
    loadConsoles()
  }, [loadConsoles])


  // create list of consoles that should be displayed
  const archivedConsoles =
    useMemo(() => {
      const normalizedSearch =
        search.trim().toLowerCase()


      return consoles
        .filter(
          (consoleItem) =>
            consoleItem.status ===
            'ARCHIVED',
        )
        .filter((consoleItem) => {
          if (!normalizedSearch) {
            return true
          }

          // fields that can be searched
          const searchableValues = [
            consoleItem.serialNumber,
            consoleItem.consoleModelName,
            consoleItem.manufacturerName,
            consoleItem.ownerName,
            consoleItem.externalOwnerName,
            consoleItem.region,
            consoleItem.color,
          ]


          // some() returs true if theres at least one
          return searchableValues.some(
            (value) =>
              String(value ?? '')
                .toLowerCase()
                .includes(
                  normalizedSearch,
                ),
          )
        })

    }, [consoles, search])


  // obtain owner name of console
  const getOwnerName = (
    consoleItem,
  ) => {
    return (
      consoleItem.ownerName ??
      consoleItem.externalOwnerName ??
      consoleItem.owner?.name ??
      'No owner'
    )
  }
  // navigate to details page
  const openConsole = (
    consoleId,
  ) => {
    navigate(
      `/consoles/${consoleId}`,
    )
  }


  return (
    <section className="mx-auto max-w-7xl">
      <button
        type="button"

        onClick={() =>
          navigate('/consoles')
        }

        className="mb-6 flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />

        <span>
          Back to consoles
        </span>
      </button>

      <div className="mb-8 flex flex-col gap-5 md:flex-row md:items-end md:justify-between">

        <div>

          <div className="flex items-center gap-3">

            <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-slate-900 text-white">
              <Archive className="h-5 w-5" />
            </div>


            <div>
              <h1 className="text-3xl font-bold text-slate-900">
                Archived Consoles
              </h1>

            </div>

          </div>

        </div>


        {/*
         *show the number of archived consoles only when loading has finished and there is no error
         */}
        {!loading && !error && (

          <div className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-medium text-slate-600 shadow-sm">

            {archivedConsoles.length}
            {' '}
            {archivedConsoles.length === 1
              ? 'console'
              : 'consoles'}

          </div>
        )}

      </div>
      <div className="mb-6 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">

        {/*
         * sr-only hides the label visually but keeps it accessible
         */}
        <label
          htmlFor="archived-console-search"
          className="sr-only"
        >
          Search archived consoles
        </label>


        <div className="relative">

          <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
          <input
            id="archived-console-search"

            type="search"

            value={search}

            onChange={(event) =>
              setSearch(
                event.target.value,
              )
            }

            placeholder="Search by model, manufacturer, serial number or owner..."

            className="w-full rounded-lg border border-slate-300 py-3 pl-12 pr-4 outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
          />

        </div>

      </div>
      {error && (

        <div
          role="alert"
          className="mb-6 rounded-xl border border-red-200 bg-red-50 p-5 text-red-700"
        >

          <p className="font-medium">
            {error}
          </p>
          <button
            type="button"

            onClick={
              loadConsoles
            }

            className="mt-4 flex items-center gap-2 text-sm font-semibold"
          >
            <RefreshCw className="h-4 w-4" />

            <span>
              Try again
            </span>
          </button>

        </div>
      )}


      {/*
       * main conditional rendering.
       */}
      {loading ? (

        <div className="flex min-h-80 items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white text-slate-500 shadow-sm">

          <LoaderCircle className="h-6 w-6 animate-spin" />

          <span>
            Loading archived consoles...
          </span>

        </div>

      ) : (

        // do not display content when an error exists
        !error && (
          <>
            {archivedConsoles.length > 0 ? (

              <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
                {/*
                 * create one card for every console, which works as a button
                 */}
                {archivedConsoles.map(
                  (consoleItem) => (

                    <article
                      key={
                        consoleItem.consoleId
                      }
                      className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:shadow-md"
                    >
                      <button
                        type="button"

                        onClick={() =>
                          openConsole(
                            consoleItem.consoleId,
                          )
                        }

                        className="block w-full text-left"
                      >
                        <div className="flex items-start justify-between gap-4 border-b border-slate-100 p-5">


                          <div className="flex min-w-0 items-center gap-3">

                            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-500">

                              <Gamepad2 className="h-5 w-5" />

                            </div>


                            <div className="min-w-0">
                              <h2 className="truncate font-semibold text-slate-900">

                                {consoleItem
                                  .consoleModelName ??
                                  'Unknown model'}

                              </h2>
                              <p className="truncate text-sm text-slate-500">

                                {consoleItem
                                  .manufacturerName ??
                                  'Unknown manufacturer'}

                              </p>

                            </div>

                          </div>

                          <span className="shrink-0 rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">

                            Archived

                          </span>

                        </div>


                        {/*
                         * dl =description list
                         * dt =description term
                         * dd =description value
                         */}
                        <dl className="grid grid-cols-2 gap-x-4 gap-y-5 p-5 text-sm">
                          <div>

                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">
                              Serial number
                            </dt>
                            <dd className="mt-1 truncate font-medium text-slate-700">
                              {consoleItem
                                .serialNumber ??
                                'Not available'}
                            </dd>
                          </div>

                          <div>

                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">
                              Owner
                            </dt>

                            <dd className="mt-1 truncate font-medium text-slate-700">

                              {getOwnerName(
                                consoleItem,
                              )}

                            </dd>

                          </div>
                          <div>

                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">
                              Region
                            </dt>

                            <dd className="mt-1 truncate font-medium text-slate-700">

                              {consoleItem.region ??
                                'Not available'}

                            </dd>

                          </div>
                          <div>

                            <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">
                              Condition
                            </dt>

                            <dd className="mt-1 truncate font-medium text-slate-700">

                              {formatEnum(
                                consoleItem.condition,
                              )}

                            </dd>

                          </div>

                        </dl>

                      </button>

                    </article>
                  ),
                )}

              </div>

            ) : (

              // No console matches
              <EmptyState
                hasSearch={
                  search.trim().length > 0
                }
                // function passed to child component so it can clear the parent´s search state
                onClearSearch={() =>
                  setSearch('')
                }

              />
            )}
          </>
        )
      )}

    </section>
  )
}


// Component displayed when there are no archived consoles to show
const EmptyState = ({
  hasSearch,
  onClearSearch,
}) => (

  <div className="flex min-h-80 flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white px-6 text-center">


    <div className="flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">

      <Archive className="h-7 w-7" />

    </div>


    <h2 className="mt-4 text-lg font-semibold text-slate-900">
      {hasSearch
        ? 'No archived consoles found'
        : 'There are no archived consoles'}

    </h2>


    <p className="mt-2 max-w-md text-sm text-slate-500">

      {hasSearch
        ? 'No archived console matches the current search.'
        : 'Consoles with the Archived status will appear here.'}

    </p>
    {hasSearch && (

      <button
        type="button"

        onClick={
          onClearSearch
        }

        className="mt-5 rounded-lg bg-slate-900 px-4 py-2 text-sm font-semibold text-white hover:bg-slate-700"
      >
        Clear search
      </button>

    )}

  </div>
)


const formatEnum = (
  value,
) => {
  if (!value) {
    return 'Not available'
  }


  return value
    .toLowerCase()
    .split('_')
    .map(
      (word) =>
        word.charAt(0).toUpperCase() +
        word.slice(1),
    )
    .join(' ')
}


export default ArchivedConsoles