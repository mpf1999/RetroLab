import {
  useEffect,
  useState,
} from 'react'
import {
  Link,
  useNavigate,
} from 'react-router-dom'
import {
  ArrowLeft,
  Building2,
  LoaderCircle,
  Plus,
  Save,
} from 'lucide-react'
import {
  createConsoleModel,
} from '../api/consoleModelApi'
import {
  createManufacturer,
  getManufacturers,
} from '../api/manufacturerApi'

import FormField from '../components/FormField'

const ConsoleModelForm = () => {
  const navigate = useNavigate()

  const [manufacturers, setManufacturers] =
    useState([])
  const [manufacturerMode, setManufacturerMode] =useState('EXISTING')
  const [formData, setFormData] = useState({
    consoleModelName: '',
    releaseYear: '',
    manufacturerId: '',
  })

  const [
    newManufacturer,
    setNewManufacturer,
  ] = useState({
    manufacturerName: '',
    countryCode: '',
  })

  const [loading, setLoading] =
    useState(true)
  const [saving, setSaving] =
    useState(false)
  const [error, setError] = useState('')

  // manufacturer loaded once when the form is mounted
  useEffect(() => {
    let componentMounted = true

    const loadManufacturers = async () => {
      setLoading(true)
      setError('')

      try {
        const manufacturerData =
          await getManufacturers()

        if (!componentMounted) {
          return
        }

        setManufacturers(
          Array.isArray(manufacturerData)
            ? manufacturerData
            : [],
        )
      } catch (requestError) {
        if (componentMounted) {
          setError(
            requestError.message ||
              'Could not load manufacturers.',
          )
        }
      }finally {
        if (componentMounted) {
          setLoading(false)
        }
      }
    }

    loadManufacturers()

    return () => {
      componentMounted = false
    }
  }, [])

  // generic input handler
  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  // update new manufacturer form, country codes normalized
  const handleManufacturerChange = (
    event,
  ) => {
    const { name, value } = event.target
    setNewManufacturer((currentData) => ({
      ...currentData,
      [name]:
        name === 'countryCode'
          ? value.toUpperCase()
          : value,
    }))
  }

  // switching manufacturer mode clears the fields belonging to the inactive option
  const changeManufacturerMode = (mode) => {
    setManufacturerMode(mode)
    setError('')
    if (mode === 'EXISTING') {
      setNewManufacturer({
        manufacturerName: '',
        countryCode: '',
      })
    } else {
      setFormData((currentData) => ({
        ...currentData,
        manufacturerId: '',
      }))
    }
  }

  // peform client-side validation before sending request, backend validation is boss
  const validateForm = () => {
    if (!formData.consoleModelName.trim()) {
      return 'Console model name is required.'
    }
    const releaseYear = Number(
      formData.releaseYear,
    )
    const currentYear =
      new Date().getFullYear()
    if (
      !Number.isInteger(releaseYear) ||
      releaseYear < 1960 ||
      releaseYear > currentYear
    ) {
      return 'Enter a valid release year.'
    }

    if (
      manufacturerMode === 'EXISTING' &&
      !formData.manufacturerId
    ) {
      return 'Select a manufacturer.'
    }

    if (
      manufacturerMode === 'NEW' &&
      !newManufacturer.manufacturerName.trim()
    ) {
      return 'Manufacturer name is required.'
    }
    if (
      manufacturerMode === 'NEW' &&
      newManufacturer.countryCode.trim().length !==
        2
    ) {
      return 'Country code must contain two letters.'
    }

    return null
  }

  // create model directly when existing manufacturer is selected or create mnauf first when new is active

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    const validationError = validateForm()

    if (validationError) {
      setError(validationError)
      return
    }
    setSaving(true)

    try {
      let manufacturerId

      if (manufacturerMode === 'EXISTING') {
        manufacturerId = Number(
          formData.manufacturerId,
        )
      } else {
        // when a new manufacturer is chosen, must be persisted first to use its id
        const createdManufacturer =
          await createManufacturer({
            manufacturerName:
              newManufacturer.manufacturerName,
            countryCode:
              newManufacturer.countryCode,
          })
        manufacturerId =
          createdManufacturer.manufacturerId
      }

      const createdModel =
        await createConsoleModel({
          consoleModelName:
            formData.consoleModelName,

          releaseYear:
            formData.releaseYear,

          manufacturerId,
        })

      navigate(
        `/console-models/${createdModel.consoleModelId}`,
        {
          replace: true,
        },
      )
    } catch (requestError) {
      setError(
        requestError.message ||
          'Could not create the console model.',
      )
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center gap-3 text-slate-500">
        <LoaderCircle className="h-6 w-6 animate-spin" />
        <span>Loading manufacturers...</span>
      </div>
    )
  }

  return (
    <section className="mx-auto max-w-3xl">
      <Link
        to="/console-models"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-800"
      >
        <ArrowLeft className="h-4 w-4" />
        <span>Back to console models</span>
      </Link>

      <div className="mt-6">
        <h1 className="text-3xl font-bold text-slate-900">
          Add Console Model
        </h1>

        <p className="mt-2 text-slate-500">
          Register a new model and assign its manufacturer.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
        >
          {error}
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        className="mt-8 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
      >
        <div className="p-6">
          <h2 className="text-lg font-semibold text-slate-900">
            Console Model Information
          </h2>

          <div className="mt-6 grid gap-6 sm:grid-cols-2">
            <FormField
              label="Console model name"
              name="consoleModelName"
              value={formData.consoleModelName}
              onChange={handleChange}
              required
              minLength={2}
              maxLength={80}
              disabled={saving}
              placeholder="Example: Game Boy Color"
            />

            <FormField
              label="Release year"
              name="releaseYear"
              type="number"
              min="1970"
              max={new Date().getFullYear()}
              value={formData.releaseYear}
              onChange={handleChange}
              required
              disabled={saving}
              placeholder="1998"
            />
          </div>
        </div>

        <div className="border-t border-slate-200 bg-slate-50 p-6">
          <div className="flex items-center gap-3">
            <Building2 className="h-5 w-5 text-primary-500" />

            <h2 className="text-lg font-semibold text-slate-900">
              Manufacturer
            </h2>
          </div>

          <p className="mt-1 text-sm text-slate-500">
            Select an existing manufacturer or register a new one.
          </p>

          <div className="mt-5 grid gap-3 sm:grid-cols-2">
            <button
              type="button"
              onClick={() =>
                changeManufacturerMode(
                  'EXISTING',
                )
              }
              disabled={saving}
              className={`rounded-lg border px-4 py-3 text-left font-medium transition-colors ${
                manufacturerMode === 'EXISTING'
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-100'
              }`}
            >
              <Building2 className="mb-2 h-5 w-5" />
              Select existing
            </button>

            <button
              type="button"
              onClick={() =>
                changeManufacturerMode('NEW')
              }
              disabled={saving}
              className={`rounded-lg border px-4 py-3 text-left font-medium transition-colors ${
                manufacturerMode === 'NEW'
                  ? 'border-primary-500 bg-primary-50 text-primary-700'
                  : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-100'
              }`}
            >
              <Plus className="mb-2 h-5 w-5" />
              Create manufacturer
            </button>
          </div>

          {manufacturerMode === 'EXISTING' ? (
            <div className="mt-6">
              <FormField
                label="Manufacturer"
                name="manufacturerId"
                value={formData.manufacturerId}
                onChange={handleChange}
                required
                disabled={saving}
              >
                <option value="">
                  Select manufacturer
                </option>

                {manufacturers.map(
                  (manufacturer) => (
                    <option
                      key={
                        manufacturer.manufacturerId
                      }
                      value={
                        manufacturer.manufacturerId
                      }
                    >
                      {
                        manufacturer.manufacturerName
                      }

                      {manufacturer.countryCode
                        ? ` (${manufacturer.countryCode})`
                        : ''}
                    </option>
                  ),
                )}
              </FormField>

              {manufacturers.length === 0 && (
                <p className="mt-2 text-sm text-amber-600">
                  No manufacturers are registered. Create one to continue.
                </p>
              )}
            </div>
          ) : (
            <div className="mt-6 grid gap-6 sm:grid-cols-[minmax(0,1fr)_160px]">
              <FormField
                label="Manufacturer name"
                name="manufacturerName"
                value={newManufacturer.manufacturerName}
                onChange={handleManufacturerChange}
                required
                disabled={saving}
                placeholder="Example: Nintendo"
              />

              <FormField
                label="Country code"
                name="countryCode"
                value={newManufacturer.countryCode}
                onChange={handleManufacturerChange}
                required
                minLength={2}
                maxLength={2}
                pattern="[A-Za-z]{2}"
                title="Country code must contain two letters"
                disabled={saving}
                placeholder="JP"
              />
            </div>
          )}
        </div>

        <div className="flex justify-end gap-3 border-t border-slate-200 px-6 py-4">
          <Link
            to="/console-models"
            className="rounded-lg border border-slate-300 px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </Link>

          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {saving ? (
              <LoaderCircle className="h-5 w-5 animate-spin" />
            ) : (
              <Save className="h-5 w-5" />
            )}

            <span>
              {saving
                ? manufacturerMode === 'NEW'
                  ? 'Creating manufacturer and model...'
                  : 'Creating model...'
                : 'Create console model'}
            </span>
          </button>
        </div>
      </form>
    </section>
  )
}


export default ConsoleModelForm