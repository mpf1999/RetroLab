import {
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react'
import { Link } from 'react-router-dom'
import {
  Search,
  Gamepad2,
  Wrench,
  Hash,
  LoaderCircle,
} from 'lucide-react'
import apiClient from '../api/apiClient'

const GlobalSearch = () => {

  //reference to the complete search container, detect clicks outisde and close search results
  const searchContainerRef = useRef(null)

  //State containing the current search query
  const [query, setQuery] = useState('')

  //data retrieved from backend, search is performed locally from these arrays
  const [consoles, setConsoles] = useState([])
  const [repairCases, setRepairCases] =
    useState([])

  const [searchOpen, setSearchOpen] =
    useState(false)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] = useState('')

  // load consoles and cases from backend, apiClient adds JWT automatically
  useEffect(() => {
    let componentMounted = true

    const loadSearchData = async () => {
      setLoading(true)
      setError('')

      try {
        const [
          consolesResponse,
          repairCasesResponse,
        ] = await Promise.all([
          apiClient.get('/consoles'),
          apiClient.get('/repair-cases'),
        ])
        
        // prevents state updates if component has already been unmounted while the requests were running
        if (!componentMounted) {
          return
        }

        //store console and repair case data
        setConsoles(
          Array.isArray(consolesResponse.data)
            ? consolesResponse.data
            : [],
        )

        setRepairCases(
          Array.isArray(repairCasesResponse.data)
            ? repairCasesResponse.data
            : [],
        )
      } catch (requestError) {
        if (!componentMounted) {
          return
        }

        setError(
          requestError.message ||
            'Could not load search data.',
        )

        setConsoles([])
        setRepairCases([])
      } finally {
        if (componentMounted) {
          setLoading(false)
        }
      }
    }

    loadSearchData()

    // cleanup function executed when the component is unmounted
    return () => {
      componentMounted = false
    }
  }, [])

  // detect clicks outside
  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (
        searchContainerRef.current &&
        !searchContainerRef.current.contains(
          event.target,
        )
      ) {
        setSearchOpen(false)
      }
    }

    // register globakl mouse event listener
    document.addEventListener(
      'mousedown',
      handleOutsideClick,
    )

    // remove when unmounted
    return () => {
      document.removeEventListener(
        'mousedown',
        handleOutsideClick,
      )
    }
  }, [])

  const normalizedQuery =
    query.trim().toLowerCase()

    // filter console list, useMemo avoids recalculating the results unless data or query changes
  const filteredConsoles = useMemo(() => {
    if (normalizedQuery.length < 2) {
      return []
    }

    return consoles
      .filter((consoleItem) => {
        //all console properties into one string
        const searchableText = [
          consoleItem.manufacturerName,
          consoleItem.consoleModelName,
          consoleItem.serialNumber,
          consoleItem.ownerName,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()

        return searchableText.includes(
          normalizedQuery,
        )
      })
      //display max 5 consoles
      .slice(0, 5)
  }, [consoles, normalizedQuery])

  //filter repair cases, same as with consoles
  const filteredRepairCases = useMemo(() => {
    if (normalizedQuery.length < 2) {
      return []
    }
    return repairCases
      .filter((repairCase) => {
        const searchableText = [
          repairCase.title,
          repairCase.description,
          repairCase.consoleModelName,
          repairCase.manufacturerName,
          repairCase.status,
        ]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()

        return searchableText.includes(
          normalizedQuery,
        )
      })
      .slice(0, 5)
  }, [repairCases, normalizedQuery])

  // Indicate if at least one console or repair case matches the query
  const hasResults =
    filteredConsoles.length > 0 ||
    filteredRepairCases.length > 0

  //update query when user writes, at least 2 chars
  const handleQueryChange = (event) => {
    const value = event.target.value

    setQuery(value)
    setSearchOpen(value.trim().length >= 2)
  }
  //reopen the search results when input receives focus and a valid query exists
  const handleFocus = () => {
    if (query.trim().length >= 2) {
      setSearchOpen(true)
    }
  }
  //close and clear after selecting
  const closeSearch = () => {
    setQuery('')
    setSearchOpen(false)
  }

  return (
    <div
      ref={searchContainerRef}
      className="relative w-full max-w-md"
    >
      <Search className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
      {/**Controlled search input, value synchronized with the query state */}
      <input
        type="search"
        value={query}
        onChange={handleQueryChange}
        onFocus={handleFocus}
        placeholder="Search consoles or repair cases..."
        autoComplete="off"
        aria-label="Search consoles or repair cases"
        aria-expanded={searchOpen}
        className="w-full rounded-lg border border-slate-300 bg-slate-50 py-2 pl-10 pr-4 text-slate-800 outline-none transition focus:border-primary-500 focus:bg-white focus:ring-2 focus:ring-primary-100"
      />

      {/**results dropdown is only rendered when searchOpen is true */}
      {searchOpen && (
        <div className="absolute left-0 right-0 top-full z-50 mt-2 max-h-[70vh] overflow-y-auto rounded-xl border border-slate-200 bg-white shadow-xl">
          {loading ? (
            <div className="flex items-center justify-center gap-3 p-8 text-slate-500">
              <LoaderCircle className="h-5 w-5 animate-spin" />
              <span>Loading search data...</span>
            </div>
          ) : error ? (
            <div className="p-6">
              <p className="text-sm font-medium text-red-600">
                Could not load search data
              </p>

              <p className="mt-1 text-xs text-slate-500">
                {error}
              </p>
            </div>
          ) : !hasResults ? (
            <div className="p-8 text-center">
              <Search className="mx-auto h-8 w-8 text-slate-300" />

              <p className="mt-3 text-sm font-medium text-slate-700">
                No results found
              </p>

              <p className="mt-1 text-xs text-slate-500">
                Try searching by model, serial number
                or repair title.
              </p>
            </div>
          ) : (
            <>
              {filteredConsoles.length > 0 && (
                <SearchSection title="Consoles">
                  {filteredConsoles.map(
                    (consoleItem) => (
                      <Link
                        key={
                          consoleItem.consoleId
                        }
                        to={`/consoles/${consoleItem.consoleId}`}
                        onClick={closeSearch}
                        className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-slate-50"
                      >
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-blue-100 text-blue-600">
                          <Gamepad2 className="h-5 w-5" />
                        </div>

                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-semibold text-slate-900">
                            {[
                              consoleItem.manufacturerName,
                              consoleItem.consoleModelName,
                            ]
                              .filter(Boolean)
                              .join(' ') ||
                              'Unknown console'}
                          </p>

                          <div className="mt-1 flex items-center gap-1 text-xs text-slate-500">
                            <Hash className="h-3 w-3 shrink-0" />

                            <span className="truncate font-mono">
                              {consoleItem.serialNumber ||
                                'No serial number'}
                            </span>
                          </div>
                        </div>
                      </Link>
                    ),
                  )}
                </SearchSection>
              )}

              {filteredRepairCases.length >
                0 && (
                <SearchSection title="Repair Cases">
                  {filteredRepairCases.map(
                    (repairCase) => (
                      <Link
                        key={
                          repairCase.repairCaseId
                        }
                        to={`/repair-cases/${repairCase.repairCaseId}`}
                        onClick={closeSearch}
                        className="flex items-center gap-3 px-4 py-3 transition-colors hover:bg-slate-50"
                      >
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-amber-100 text-amber-600">
                          <Wrench className="h-5 w-5" />
                        </div>

                        <div className="min-w-0 flex-1">
                          <p className="truncate text-sm font-semibold text-slate-900">
                            {repairCase.title ||
                              `Repair Case #${repairCase.repairCaseId}`}
                          </p>

                          <p className="mt-1 truncate text-xs text-slate-500">
                            {[
                              repairCase.manufacturerName,
                              repairCase.consoleModelName,
                            ]
                              .filter(Boolean)
                              .join(' ') ||
                              'Unknown console'}

                            {repairCase.status && (
                              <>
                                {' · '}
                                {formatStatus(
                                  repairCase.status,
                                )}
                              </>
                            )}
                          </p>
                        </div>
                      </Link>
                    ),
                  )}
                </SearchSection>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

//reusable component used to group search results by resource type
const SearchSection = ({
  title,
  children,
}) => {
  return (
    <section className="border-b border-slate-200 last:border-b-0">
      <div className="bg-slate-50 px-4 py-2">
        <h2 className="text-xs font-semibold uppercase tracking-wider text-slate-500">
          {title}
        </h2>
      </div>

      <div className="divide-y divide-slate-100">
        {children}
      </div>
    </section>
  )
}

const formatStatus = (status) => {
  if (!status) {
    return ''
  }

  return String(status)
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (character) =>
      character.toUpperCase(),
    )
}

export default GlobalSearch