import {
  useEffect,
  useMemo,
  useState,
} from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  Camera,
  LoaderCircle,
  RefreshCw,
  Save,
  X,
} from 'lucide-react'

import {
  currencyCodes,
  getCurrencyLabel,
  getPreferredCurrency,
} from '../utils/currencies'

import {
  CONSOLE_CONDITIONS,
  CONSOLE_STATUSES,
} from '../constants/consoleOptions'

import { createConsole } from '../api/consoleApi'
import {
  getConsoleModels,
} from '../api/consoleModelApi'
import { getUsers } from '../api/userApi'
import FormField from '../components/FormField'
import getErrorMessage from '../utils/getErrorMessage'
import {
  buildConsoleRequest,
  validateConsoleForm,
} from '../utils/consoleFormUtils'

const AddConsole = () => {
  const navigate = useNavigate()

  // lazy initialization reads the preferred currency only once
  const [formData, setFormData] = useState({
    ownerType: 'TEAM_MEMBER',
    ownerId: '',
    ownerName: '',
    consoleModelId: '',
    serialNumber: '',
    region: '',
    color: '',
    condition: '',
    status: '',
    estimatedValue: {
      amount: '',
      currency:
        getPreferredCurrency() || 'EUR',
    },
    notes: '',
  })

  const [teamMembers, setTeamMembers] =
    useState([])

  const [consoleModels, setConsoleModels] =
    useState([])

  const [image, setImage] = useState(null)

  const [imagePreview, setImagePreview] =
    useState(null)

  const [loadingOptions, setLoadingOptions] =
    useState(true)

  const [submitting, setSubmitting] =
    useState(false)

  const [error, setError] = useState('')

  // loads users and console models required. Promise.all runs both independent API requests concurrently
  const loadOptions = async () => {
    try {
      setLoadingOptions(true)
      setError('')

      const [
        usersData,
        consoleModelsData,
      ] = await Promise.all([
        getUsers(),
        getConsoleModels(),
      ])

      setTeamMembers(
        Array.isArray(usersData) ? usersData : [],
      )

      setConsoleModels(
        Array.isArray(consoleModelsData) ? consoleModelsData : [],
      )
    } catch (requestError) {
      setTeamMembers([])
      setConsoleModels([])

      setError(
        getErrorMessage(
          requestError,
          'Could not load users and console models.',
        ),
      )
    } finally {
      setLoadingOptions(false)
    }
  }

  useEffect(() => {
    loadOptions()
  }, [])
  // object URLs are temporary browser resources, releasing the current prevents the preview from remaining in memory
  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview)
      }
    }
  }, [imagePreview])

  // User id or id so it is the same as the backend DTO
  const normalizedTeamMembers =
    useMemo(() => {
      return teamMembers
        .map((member) => {
          const userId =
            member.userId ?? member.id

          return {
            userId,

            name:
              member.name ??
              member.email ??
              `User ${userId}`,
          }
        })
        .filter(
          (member) =>
            member.userId !== null &&
            member.userId !== undefined,
        )
    }, [teamMembers])

  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  // a console can have a registered owner or an external one, withching clears the other
  const handleOwnerTypeChange = (
    event,
  ) => {
    const ownerType = event.target.value

    setFormData((currentData) => ({
      ...currentData,
      ownerType,
      ownerId: '',
      ownerName: '',
    }))
  }

  const handleEstimatedValueChange = (
    event,
  ) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,

      estimatedValue: {
        ...currentData.estimatedValue,
        [name]: value,
      },
    }))
  }

  // perform immediate client-side checks to match the image formats and max size accepted by the backend
  const handleImageChange = (event) => {
    const selectedImage =
      event.target.files?.[0]

    if (!selectedImage) {
      return
    }

    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'image/webp',
    ]

    if (
      !allowedTypes.includes(
        selectedImage.type,
      )
    ) {
      setError(
        'Only JPG, PNG and WebP images are allowed.',
      )

      event.target.value = ''
      return
    }

    // 5mb
    const maximumSize =
      5 * 1024 * 1024

    if (
      selectedImage.size > maximumSize
    ) {
      setError(
        'The uploaded image exceeds the allowed size.',
      )

      event.target.value = ''
      return
    }

    if (imagePreview) {
      URL.revokeObjectURL(imagePreview)
    }

    setError('')
    setImage(selectedImage)

    setImagePreview(
      URL.createObjectURL(selectedImage),
    )
  }

  const removeImage = () => {
    if (imagePreview) {
      URL.revokeObjectURL(imagePreview)
    }

    setImage(null)
    setImagePreview(null)
  }

  // frohntend validation improves feedback, but does not replace backend validation
  const validateForm = () =>
    validateConsoleForm(formData)

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    const validationError =validateForm()

    if (validationError) {
      setError(validationError)
      return
    }

    try {
      setSubmitting(true)

      // build JSON shape expected. values from HTML inputs are strings, so they need to be converted for ids and Money

      const consoleRequest =
        buildConsoleRequest(formData)

      /*
       * createConsole executes:
       * 1. POST /consoles with JSON
       * 2. POST /consoles/{id}/image if there is
       */
      const createdConsole =
        await createConsole(
          consoleRequest,
          image,
        )

      navigate(
        `/consoles/${createdConsole.consoleId}`,
        {
          replace: true,
        },
      )
    } catch (requestError) {
      console.error(
        'Console creation failed:',
        requestError.response?.data ??
          requestError,
      )

      setError(
        getErrorMessage(
          requestError,
          'Could not create the console.',
        ),
      )
    } finally {
      setSubmitting(false)
    }
  }
  //Submission is unavailable while a request is running or when the selected owner mode has no usable backend options

    const cannotSubmit =
      submitting ||
      consoleModels.length === 0 ||
      (
        formData.ownerType ===
          'TEAM_MEMBER' &&
        normalizedTeamMembers.length === 0
      )
      
  return (
    <section className="mx-auto max-w-4xl">
      <button
        type="button"
        onClick={() =>
          navigate('/consoles')
        }
        className="mb-6 flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />

        <span>Back to consoles</span>
      </button>

      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900">
          Add Console
        </h1>

        <p className="mt-2 text-slate-500">
          Register a new console in RetroLab.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
        >
          <p>{error}</p>

          {!loadingOptions &&
            consoleModels.length === 0 && (
              <button
                type="button"
                onClick={loadOptions}
                className="mt-3 flex items-center gap-2 font-semibold"
              >
                <RefreshCw className="h-4 w-4" />

                <span>
                  Try loading again
                </span>
              </button>
            )}
        </div>
      )}

      {loadingOptions ? (
        <div className="flex min-h-80 items-center justify-center gap-3 rounded-xl border border-slate-200 bg-white text-slate-500 shadow-sm">
          <LoaderCircle className="h-6 w-6 animate-spin" />

          <span>
            Loading form options...
          </span>
        </div>
      ) : (
        <form
          onSubmit={handleSubmit}
          className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
        >
          {/*Image */}
          <div className="border-b border-slate-200 p-6">
            <h2 className="text-lg font-semibold text-slate-900">
              Console image
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              The image is optional. Maximum
              size: 5 MB.
            </p>

            <div className="mt-5">
              {imagePreview ? (
                <div className="relative h-52 max-w-sm overflow-hidden rounded-xl border border-slate-200">
                  <img
                    src={imagePreview}
                    alt="Console preview"
                    className="h-full w-full object-cover"
                  />

                  <button
                    type="button"
                    onClick={removeImage}
                    aria-label="Remove image"
                    disabled={submitting}
                    className="absolute right-2 top-2 flex h-9 w-9 items-center justify-center rounded-full bg-slate-900/80 text-white hover:bg-slate-900 disabled:opacity-60"
                  >
                    <X className="h-5 w-5" />
                  </button>
                </div>
              ) : (
                <label
                  htmlFor="console-image"
                  className="flex h-52 max-w-sm cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-slate-300 bg-slate-50 text-slate-500 transition hover:border-primary-500 hover:bg-primary-50 hover:text-primary-600"
                >
                  <Camera className="h-8 w-8" />

                  <span className="mt-3 font-medium">
                    Select an image
                  </span>

                  <span className="mt-1 text-xs">
                    JPG, PNG or WebP
                  </span>
                </label>
              )}

              <input
                id="console-image"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                onChange={handleImageChange}
                disabled={submitting}
                className="hidden"
              />
            </div>
          </div>

          {/* Information */}
          <div className="p-6">
            <h2 className="text-lg font-semibold text-slate-900">
              Console information
            </h2>

            <div className="mt-6 grid gap-6 sm:grid-cols-2">
              <FormField
                label="Owner type"
                name="ownerType"
                value={formData.ownerType}
                onChange={
                  handleOwnerTypeChange
                }
                disabled={submitting}
              >
                <option value="TEAM_MEMBER">
                  Team member
                </option>

                <option value="CLIENT">
                  External client
                </option>
              </FormField>

              {formData.ownerType ===
              'TEAM_MEMBER' ? (
                <FormField
                  label="Team member"
                  name="ownerId"
                  value={formData.ownerId}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">
                    Select a team member
                  </option>

                  {normalizedTeamMembers.map(
                    (member) => (
                      <option
                        key={member.userId}
                        value={member.userId}
                      >
                        {member.name}
                      </option>
                    ),
                  )}
                </FormField>
              ) : (
                <FormField
                  label="Client name"
                  name="ownerName"
                  value={formData.ownerName}
                  onChange={handleChange}
                  required
                  maxLength={100}
                  disabled={submitting}
                />
              )}

              <FormField
                label="Console model"
                name="consoleModelId"
                value={
                  formData.consoleModelId
                }
                onChange={handleChange}
                required
                disabled={submitting}
              >
                <option value="">
                  Select a console model
                </option>

                {consoleModels.map(
                  (model) => (
                    <option
                      key={
                        model.consoleModelId
                      }
                      value={
                        model.consoleModelId
                      }
                    >
                      {model.manufacturerName}
                      {' — '}
                      {model.consoleModelName}
                    </option>
                  ),
                )}
              </FormField>

              <FormField
                label="Serial number"
                name="serialNumber"
                value={
                  formData.serialNumber
                }
                onChange={handleChange}
                required
                maxLength={45}
                disabled={submitting}
              />

              <FormField
                label="Region"
                name="region"
                value={formData.region}
                onChange={handleChange}
                required
                disabled={submitting}
              />

              <FormField
                label="Color"
                name="color"
                value={formData.color}
                onChange={handleChange}
                required
                maxLength={30}
                disabled={submitting}
              />

              <FormField
                label="Condition"
                name="condition"
                value={formData.condition}
                onChange={handleChange}
                required
                disabled={submitting}>
                <option value="">
                  Select a condition
                </option>

                {CONSOLE_CONDITIONS.map(
                  (condition) => (
                    <option
                      key={condition.value}
                      value={condition.value}
                    >
                      {condition.label}
                    </option>
                  ),
                )}
              </FormField>
              <FormField
                  label="Status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  required
                  disabled={submitting}
                >
                  <option value="">
                    Select a status
                  </option>

                  {CONSOLE_STATUSES.map(
                    (status) => (
                      <option
                        key={status.value}
                        value={status.value}
                      >
                        {status.label}
                      </option>
                    ),
                  )}
                </FormField>

              {/* MoneyDTO */}
              <div className="sm:col-span-2">
                <label
                  htmlFor="amount"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Estimated value
                </label>

                <div className="flex flex-col gap-2 sm:flex-row">
                  <input
                    id="amount"
                    name="amount"
                    type="number"
                    min="0"
                    step="0.01"
                    value={
                      formData
                        .estimatedValue
                        .amount
                    }
                    onChange={
                      handleEstimatedValueChange
                    }
                    required
                    disabled={submitting}
                    placeholder="0.00"
                    className="min-w-0 flex-1 rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:bg-slate-100 disabled:opacity-60"
                  />

                  <select
                    name="currency"
                    value={
                      formData
                        .estimatedValue
                        .currency
                    }
                    onChange={
                      handleEstimatedValueChange
                    }
                    disabled={submitting}
                    className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:bg-slate-100 disabled:opacity-60 sm:w-64"
                  >
                    {currencyCodes.map(
                      (currencyCode) => (
                        <option
                          key={currencyCode}
                          value={currencyCode}
                        >
                          {getCurrencyLabel(
                            currencyCode,
                          )}
                        </option>
                      ),
                    )}
                  </select>
                </div>
              </div>
            </div>

            <div className="mt-6">
              <label
                htmlFor="notes"
                className="mb-2 block text-sm font-medium text-slate-700"
              >
                Notes
              </label>

              <textarea
                id="notes"
                name="notes"
                rows={5}
                maxLength={2000}
                value={formData.notes}
                onChange={handleChange}
                disabled={submitting}
                className="w-full resize-y rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:bg-slate-100 disabled:opacity-60"
              />
            </div>
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-200 bg-slate-50 px-6 py-4">
            <button
              type="button"
              onClick={() =>
                navigate('/consoles')
              }
              disabled={submitting}
              className="rounded-lg border border-slate-300 bg-white px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-60"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {submitting ? (
                <LoaderCircle className="h-5 w-5 animate-spin" />
              ) : (
                <Save className="h-5 w-5" />
              )}

              <span>
                {submitting
                  ? 'Saving...'
                  : 'Save console'}
              </span>
            </button>
          </div>
        </form>
      )}
    </section>
  )
}

export default AddConsole