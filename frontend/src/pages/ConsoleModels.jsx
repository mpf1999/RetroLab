import {
  useEffect,
  useMemo,
  useState,
} from 'react'
import { Link } from 'react-router-dom'
import {
  Boxes,
  CalendarDays,
  Cpu,
  LoaderCircle,
  Plus,
  RefreshCw,
} from 'lucide-react'
import {
  getConsoleModels,
} from '../api/consoleModelApi'
import {
  getComponents,
} from '../api/componentApi'
import SearchField from '../components/SearchField'
import Pagination from '../components/Pagination'
import getErrorMessage from '../utils/getErrorMessage'

const ConsoleModels = () => {
  const [consoleModels, setConsoleModels] =
    useState([])
  const [components, setComponents] =
    useState([])

  // search filter and pagination
  const [searchText, setSearchText] =
    useState('')

  const [
    selectedManufacturerId,
    setSelectedManufacturerId,
  ] = useState('')

  const [itemsPerPage, setItemsPerPage] =
    useState(10)
  const [currentPage, setCurrentPage] =
    useState(1)

  const [loading, setLoading] =
    useState(true)

  const [error, setError] = useState('')

  // concurrent load
  const loadData = async () => {
    setLoading(true)
    setError('')

    try {
      const [
        modelsData,
        componentsData,
      ] = await Promise.all([
        getConsoleModels(),
        getComponents(),
      ])
      setConsoleModels(
        Array.isArray(modelsData)
          ? modelsData
          : [],
      )
      setComponents(
        Array.isArray(componentsData)
          ? componentsData
          : [],
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not load console models.',
        ),
      )
    } finally {
      setLoading(false)
    }
  }

  // load data once page is mounted
  useEffect(() => {
    loadData()
  }, [])

  // build object containing number of components, useMemo avoids recalculating
  const componentCountByModel =
    useMemo(() => {
      return components.reduce(
        (counts, component) => {
          const modelId =
            component.consoleModelId
          counts[modelId] =
            (counts[modelId] ?? 0) + 1
          return counts
        },
        {},
      )
    }, [components])

  // manufacturer filter options from the loaded models, map to avoid duplicate before sorting
  const manufacturers = useMemo(() => {
    const manufacturerMap = new Map()

    consoleModels.forEach((model) => {
      if (
        model.manufacturerId === null ||
        model.manufacturerId === undefined
      ) {
        return
      }
      manufacturerMap.set(
        String(model.manufacturerId),
        {
          manufacturerId:
            model.manufacturerId,
          manufacturerName:
            model.manufacturerName,
        },
      )
    })

    return Array.from(
      manufacturerMap.values(),
    ).sort((first, second) =>
      (first.manufacturerName ?? '')
        .localeCompare(
          second.manufacturerName ?? '',
        ),
    )
  }, [consoleModels])

  // apply text search and filter
  const filteredModels = useMemo(() => {
    const query = searchText
      .trim()
      .toLowerCase()

    return consoleModels.filter((model) => {
      const matchesManufacturer =
        !selectedManufacturerId ||
        String(model.manufacturerId) ===
          selectedManufacturerId

      const searchableText = [
        model.consoleModelName,
        model.manufacturerName,
        model.releaseYear,
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()

      return (
        matchesManufacturer &&
        searchableText.includes(query)
      )
    })
  }, [
    consoleModels,
    searchText,
    selectedManufacturerId,
  ])

  // calculate pagination values

  const totalPages = Math.ceil(
    filteredModels.length / itemsPerPage,
  )

  const firstIndex =
    (currentPage - 1) * itemsPerPage

  const lastIndex = firstIndex + itemsPerPage

  const visibleModels = filteredModels.slice(
    firstIndex,
    lastIndex,
  )

  const firstVisibleItem =
    filteredModels.length === 0
      ? 0
      : firstIndex + 1

  const lastVisibleItem = Math.min(
    lastIndex,
    filteredModels.length,
  )

  return (
    <section>
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">
            Console Models
          </h1>

          <p className="mt-2 text-slate-500">
            Manage console models and their
            registered components.
          </p>
        </div>

        <Link
          to="/console-models/new"
          className="flex items-center justify-center gap-2 rounded-lg bg-primary-500 px-4 py-3 font-semibold text-white hover:bg-primary-600"
        >
          <Plus className="h-5 w-5" />
          <span>Add console model</span>
        </Link>
      </div>

      {/* Search, manufacturer filter and pagination-size controls. */}
      <div className="mt-8 grid gap-4 rounded-xl border border-slate-200 bg-white p-4 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-end">
        <div className="w-full sm:w-80 lg:w-96">
          <SearchField
            id="model-search"
            value={searchText}
            onChange={(value) => {
              setSearchText(value)
              setCurrentPage(1)
            }}
            placeholder="Model, manufacturer or year..."
          />
        </div>

        <div className="flex flex-col gap-3 sm:flex-row sm:items-end lg:justify-end">
          <div>
            <label htmlFor="manufacturer-filter" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Manufacturer
            </label>
            <select
              id="manufacturer-filter"
              value={selectedManufacturerId}
              onChange={(event) => {
                setSelectedManufacturerId(event.target.value)
                setCurrentPage(1)
              }}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
            >
              <option value="">All manufacturers</option>
              {manufacturers.map((manufacturer) => (
                <option
                  key={manufacturer.manufacturerId}
                  value={manufacturer.manufacturerId}
                >
                  {manufacturer.manufacturerName}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="models-per-page" className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500">
              Items per page
            </label>
            <select
              id="models-per-page"
              value={itemsPerPage}
              onChange={(event) => {
                setItemsPerPage(Number(event.target.value))
                setCurrentPage(1)
              }}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
            >
              <option value={10}>10</option>
              <option value={25}>25</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="mt-6 flex items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white p-12 text-slate-500">
          <LoaderCircle className="h-6 w-6 animate-spin" />
          <span>Loading console models...</span>
        </div>
      ) : error ? (
        <div className="mt-6 rounded-xl border border-red-200 bg-white p-12 text-center">
          <p className="text-red-600">
            {error}
          </p>

          <button
            type="button"
            onClick={loadData}
            className="mx-auto mt-4 flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2"
          >
            <RefreshCw className="h-4 w-4" />
            <span>Try again</span>
          </button>
        </div>
      ) : visibleModels.length > 0 ? (
        <div className="mt-6 grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          {visibleModels.map((model) => (
            <ConsoleModelCard
              key={model.consoleModelId}
              model={model}
              componentCount={
                componentCountByModel[
                  model.consoleModelId
                ] ?? 0
              }
            />
          ))}
        </div>
      ) : (
        <div className="mt-6 rounded-xl border border-slate-200 bg-white p-12 text-center">
          <Boxes className="mx-auto h-12 w-12 text-slate-300" />

          <h2 className="mt-4 font-semibold text-slate-800">
            No console models found
          </h2>
        </div>
      )}

      {/* Pagination is available only after a successful request. */}
      {!loading && !error && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      )}
    </section>
  )
}

// reusable card representing one console model
const ConsoleModelCard = ({
  model,
  componentCount,
}) => (
  <Link
    to={`/console-models/${model.consoleModelId}`}
    className="group rounded-xl border border-slate-200 bg-white p-6 shadow-sm transition-all hover:-translate-y-1 hover:border-primary-300 hover:shadow-md"
  >
    <div className="flex justify-between">
      <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
        <Boxes className="h-6 w-6" />
      </div>

      <span className="rounded-full bg-slate-100 px-3 py-3.5 text-xs font-semibold text-slate-600">
        {model.manufacturerName}
      </span>
    </div>

    <h2 className="mt-5 text-xl font-semibold text-slate-900 group-hover:text-primary-600">
      {model.consoleModelName}
    </h2>

    <div className="mt-5 flex justify-between border-t border-slate-100 pt-4 text-sm text-slate-500">
      <span className="flex items-center gap-2">
        <CalendarDays className="h-4 w-4" />
        {model.releaseYear}
      </span>

      <span className="flex items-center gap-2">
        <Cpu className="h-4 w-4" />
        {componentCount}{' '}
        {componentCount === 1
          ? 'component'
          : 'components'}
      </span>
    </div>
  </Link>
)

export default ConsoleModels