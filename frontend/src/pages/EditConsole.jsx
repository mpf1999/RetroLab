import {
  useEffect,
  useMemo,
  useState,
} from 'react'
import {
  useNavigate,
  useParams,
} from 'react-router-dom'
import {
  ArrowLeft,
  Gamepad2,
  ImagePlus,
  LoaderCircle,
  Save,
  X,
} from 'lucide-react'
import {
  currencyCodes,
  getCurrencyLabel,
} from '../utils/currencies'
import {
  getConsoleById,
  updateConsole,
} from '../api/consoleApi'
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

const initialFormData = {
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
    currency: 'EUR',
  },
  notes: '',
}

const EditConsole = () => {
  const { id } = useParams()
  const navigate = useNavigate()

  const [formData, setFormData] =
    useState(initialFormData)
  const [consoleModels, setConsoleModels] =useState([])
  const [teamMembers, setTeamMembers] =
    useState([])

  const [currentImageUrl, setCurrentImageUrl] =
    useState(null)

  const [selectedImage, setSelectedImage] =
    useState(null)

  const [imagePreview, setImagePreview] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [saving, setSaving] =
    useState(false)

  const [error, setError] = useState('')

  //load console with console models and users
  useEffect(() => {
    let componentMounted = true

    const loadEditData = async () => {
      setLoading(true)
      setError('')

      try {
        const [
          consoleData,
          consoleModelsData,
          usersData,
        ] = await Promise.all([
          getConsoleById(id),
          getConsoleModels(),
          getUsers(),
        ])

        if (!componentMounted) {
          return
        }
        setConsoleModels(
          Array.isArray(consoleModelsData)
            ? consoleModelsData
            : [],
        )

        setTeamMembers(
          Array.isArray(usersData)
            ? usersData
            : [],
        )

        setCurrentImageUrl(
          consoleData.imageUrl ?? null,
        )

        setFormData({
          ownerType: consoleData.ownerId
            ? 'TEAM_MEMBER'
            : 'CLIENT',

          ownerId:
            consoleData.ownerId?.toString() ??
            '',

          ownerName: consoleData.ownerId
            ? ''
            : consoleData.ownerName ?? '',

          consoleModelId:
            consoleData.consoleModelId?.toString() ??
            '',
          serialNumber:
            consoleData.serialNumber ?? '',

          region: consoleData.region ?? '',
          color: consoleData.color ?? '',
          condition:
            consoleData.condition ?? '',
          status: consoleData.status ?? '',
          estimatedValue: {
            amount:
              consoleData.estimatedValue?.amount ??
              '',

            currency:
              consoleData.estimatedValue
                ?.currency ?? 'EUR',
          },

          notes: consoleData.notes ?? '',
        })
      } catch (requestError) {
        if (componentMounted) {
          setError(
            getErrorMessage(
              requestError,
              'Could not load the console.',
            ),
          )
        }
      } finally {
        if (componentMounted) {
          setLoading(false)
        }
      }
    }

    loadEditData()

    return () => {
      componentMounted = false
    }
  }, [id])

  // object URLs are temporary browser resources, so current preview URL is release when its replaced or page unmounts
  useEffect(() => {
    return () => {
      if (imagePreview) {
        URL.revokeObjectURL(imagePreview)
      }
    }
  }, [imagePreview])


  const normalizedTeamMembers = useMemo(() => {
    return teamMembers.map((member) => ({
      userId:
        member.userId ?? member.id,

      name:
        member.name ??
        member.email ??
        `User ${member.userId ?? member.id}`,
    }))
  }, [teamMembers])

  // generic handler
  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  // adapt to the two types of owner, clear the other
  const handleOwnerTypeChange = (event) => {
    setFormData((currentData) => ({
      ...currentData,
      ownerType: event.target.value,
      ownerId: '',
      ownerName: '',
    }))
  }

  const handleMoneyChange = (event) => {
    const { name, value } = event.target
    setFormData((currentData) => ({
      ...currentData,
      estimatedValue: {
        ...currentData.estimatedValue,
        [name]: value,
      },
    }))
  }

  // store selected img and create a temporary local URL so the user can preview it before sumitting
  const handleImageChange = (event) => {
    const image =
      event.target.files?.[0] ?? null

    if (imagePreview) {
      URL.revokeObjectURL(imagePreview)
    }

    setSelectedImage(image)

    setImagePreview(
      image
        ? URL.createObjectURL(image)
        : null,
    )
  }
  const removeSelectedImage = () => {
    if (imagePreview) {
      URL.revokeObjectURL(imagePreview)
    }

    setSelectedImage(null)
    setImagePreview(null)
  }

  // same client-side validation rules as add console form
  const validateForm = () =>
    validateConsoleForm(formData)
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
      const requestData =
        buildConsoleRequest(formData)

      const updatedConsole =
        await updateConsole(
          id,
          requestData,
          selectedImage,
        )

      navigate(
        `/consoles/${updatedConsole.consoleId}`,
        {
          replace: true,
        },
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not update the console.',
        ),
      )
    } finally {
      setSaving(false)
    }
  }

  // do not render until its initial data is available
  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-slate-500">
        <LoaderCircle className="h-6 w-6 animate-spin" />
        <span>Loading console...</span>
      </div>
    )
  }

  return (
    <section className="mx-auto max-w-4xl">
      <button
        type="button"
        onClick={() =>
          navigate(`/consoles/${id}`)
        }
        className="mb-6 flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-800"
      >
        <ArrowLeft className="h-4 w-4" />
        <span>Back to console</span>
      </button>

      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900">
          Edit Console
        </h1>
        <p className="mt-2 text-slate-500">
          Update the console information.
        </p>
      </div>

      {error && (
        <div
          role="alert"
          className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
        >
          {error}
        </div>
      )}
      <form
        onSubmit={handleSubmit}
        className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
      >
        <div className="grid gap-6 sm:grid-cols-2">
          <FormField
            label="Owner type"
            name="ownerType"
            value={formData.ownerType}
            onChange={handleOwnerTypeChange}
            disabled={saving}
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
              disabled={saving}
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
              disabled={saving}
            />
          )}

          <FormField
            label="Console model"
            name="consoleModelId"
            value={formData.consoleModelId}
            onChange={handleChange}
            required
            disabled={saving}
          >
            <option value="">
              Select a console model
            </option>

            {consoleModels.map((model) => (
              <option
                key={model.consoleModelId}
                value={model.consoleModelId}
              >
                {model.manufacturerName} —{' '}
                {model.consoleModelName}
              </option>
            ))}
          </FormField>

          <FormField
            label="Serial number"
            name="serialNumber"
            value={formData.serialNumber}
            onChange={handleChange}
            required
            disabled={saving}
          />
          <FormField
            label="Region"
            name="region"
            value={formData.region}
            onChange={handleChange}
            required
            disabled={saving}
          />
          <FormField
            label="Color"
            name="color"
            value={formData.color}
            onChange={handleChange}
            required
            disabled={saving}
          />
          <FormField
            label="Condition"
            name="condition"
            value={formData.condition}
            onChange={handleChange}
            required
            disabled={saving}
          >
            <option value="">
              Select condition
            </option>

            <option value="EXCELLENT">
              Excellent
            </option>

            <option value="GOOD">Good</option>
            <option value="FAIR">Fair</option>
            <option value="POOR">Poor</option>
            <option value="BROKEN">
              Broken
            </option>
          </FormField>

          <FormField
            label="Status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            required
            disabled={saving}
          >
            <option value="">
              Select status
            </option>

            <option value="REGISTERED">
              Registered
            </option>

            <option value="IN_REPAIR">
              In repair
            </option>

            <option value="REPAIRED">
              Repaired
            </option>

            <option value="ARCHIVED">
              Archived
            </option>
          </FormField>

          {/* Estimated value stored as a MoneyDTO. */}
          <div className="sm:col-span-2">
            <label
              htmlFor="estimated-amount"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Estimated value
            </label>

            <div className="flex flex-col gap-2 sm:flex-row">
              <input
                id="estimated-amount"
                name="amount"
                type="number"
                min="0"
                step="0.01"
                value={
                  formData.estimatedValue.amount
                }
                onChange={handleMoneyChange}
                required
                disabled={saving}
                className="min-w-0 flex-1 rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:opacity-60"
              />

              <select
                name="currency"
                value={
                  formData.estimatedValue.currency
                }
                onChange={handleMoneyChange}
                disabled={saving}
                className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:opacity-60 sm:w-64"
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

          <div className="sm:col-span-2">
            <label
              htmlFor="console-image"
              className="mb-2 block text-sm font-medium text-slate-700"
            >
              Console image
            </label>

            <div className="rounded-xl border border-dashed border-slate-300 p-5">
              <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
                <div className="flex h-32 w-full shrink-0 items-center justify-center overflow-hidden rounded-lg bg-slate-100 sm:w-48">
                  {imagePreview ||
                  currentImageUrl ? (
                    <img
                      src={
                        imagePreview ||
                        currentImageUrl
                      }
                      alt="Console preview"
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <Gamepad2 className="h-12 w-12 text-slate-300" />
                  )}
                </div>

                <div>
                  <label
                    htmlFor="console-image"
                    className="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100"
                  >
                    <ImagePlus className="h-4 w-4" />

                    <span>
                      {selectedImage
                        ? 'Choose another image'
                        : 'Choose new image'}
                    </span>
                  </label>

                  <input
                    id="console-image"
                    type="file"
                    accept="image/png,image/jpeg,image/webp"
                    onChange={handleImageChange}
                    disabled={saving}
                    className="sr-only"
                  />

                  <p className="mt-2 text-xs text-slate-500">
                    PNG, JPG or WebP.
                  </p>

                  {selectedImage && (
                    <button
                      type="button"
                      onClick={
                        removeSelectedImage
                      }
                      className="mt-2 flex items-center gap-1 text-sm font-medium text-red-600"
                    >
                      <X className="h-4 w-4" />
                      <span>
                        Keep current image
                      </span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="mt-6">
          <FormField
            label="Notes"
            name="notes"
            value={formData.notes}
            onChange={handleChange}
            textarea
            rows={5}
            disabled={saving}
          />
        </div>

        <div className="mt-8 flex justify-end gap-3 border-t border-slate-200 pt-6">
          <button
            type="button"
            onClick={() =>
              navigate(`/consoles/${id}`)
            }
            disabled={saving}
            className="rounded-lg border border-slate-300 px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-60"
          >
            Cancel
          </button>

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
                ? 'Saving...'
                : 'Save changes'}
            </span>
          </button>
        </div>
      </form>
    </section>
  )
}

export default EditConsole