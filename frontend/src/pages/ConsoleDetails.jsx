import {
  useEffect,
  useState,
} from 'react'
import {
  Link,
  useNavigate,
  useParams,
} from 'react-router-dom'
import {
  AlertCircle,
  ArrowLeft,
  CircleDollarSign,
  Gamepad2,
  Hash,
  LoaderCircle,
  MapPin,
  Palette,
  Pencil,
  ShieldCheck,
  Trash2,
  User,
} from 'lucide-react'
import {
  deleteConsole,
  getConsoleById,
} from '../api/consoleApi'
import {
  resolveImageUrl,
} from '../utils/imageUrl'
import DeleteConfirmationModal from '../components/DeleteConfirmationModal'

const ConsoleDetails = () => {
  // console id obtained from current route, navigate to go back to list after delete
  const { id } = useParams()
  const navigate = useNavigate()

  const [consoleItem, setConsoleItem] =
    useState(null)

  // request states to have visual feedback in load and delete
  const [loading, setLoading] =
    useState(true)
  const [deleting, setDeleting] =
    useState(false)

  const [error, setError] = useState('')

  const [imageError, setImageError] =
    useState(false)

  // controls reusable confirmation modal before delete

  const [
    deleteConfirmationOpen,
    setDeleteConfirmationOpen,
  ] = useState(false)

  // get selected console whenever route id changes, reset img errors
  useEffect(() => {
    const loadConsole = async () => {
      try {
        setLoading(true)
        setError('')
        setImageError(false)

        const data =
          await getConsoleById(id)

        setConsoleItem(data)
      } catch (requestError) {
        setError(
          requestError.userMessage ??
            requestError.message ??
            'Could not load the console.',
        )
      } finally {
        setLoading(false)
      }
    }

    loadConsole()
  }, [id])
  const handleDelete = () => {
    setDeleteConfirmationOpen(true)
  }

  // delete console after confirm
  const confirmDelete = async () => {
    try {
      setDeleting(true)
      setError('')

      await deleteConsole(id)
      navigate('/consoles', {
        replace: true,
      })
    } catch (requestError) {
      setError(
        requestError.userMessage ??
          requestError.message ??
          'Could not delete the console.',
      )
    } finally {
      setDeleting(false)
      setDeleteConfirmationOpen(false)
    }
  }

  // avoid rendering incomplete info while initial request is being processed
  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center">
        <LoaderCircle className="h-9 w-9 animate-spin text-primary-500" />
      </div>
    )
  }

  // error failed request
  if (!consoleItem) {
    return (
      <section className="mx-auto max-w-6xl">
        <ErrorMessage
          message={
            error || 'Console not found.'
          }
        />

        <Link
          to="/consoles"
          className="mt-6 inline-flex items-center gap-2 text-sm font-medium text-primary-600"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to consoles
        </Link>
      </section>
    )
  }

  // image path into URL
  const imageUrl = resolveImageUrl(
    consoleItem.imageUrl,
  )
  const hasImage =
    Boolean(imageUrl) && !imageError

  return (
    <section className="mx-auto max-w-6xl">
      <Link
        to="/consoles"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition-colors hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />
        Back to consoles
      </Link>

      {error && (
        <div className="mt-6">
          <ErrorMessage message={error} />
        </div>
      )}

      <div className="mt-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div>
          <p className="font-medium text-primary-600">
            {consoleItem.manufacturerName}
          </p>

          <h1 className="mt-1 text-3xl font-bold text-slate-900">
            {consoleItem.consoleModelName}
          </h1>

          <div className="mt-3 flex items-center gap-2 text-sm text-slate-500">
            <Hash className="h-4 w-4" />

            <span className="font-mono">
              {consoleItem.serialNumber}
            </span>
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          <Link
            to={`/consoles/${consoleItem.consoleId}/edit`}
            className="flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 transition-colors hover:bg-slate-100"
          >
            <Pencil className="h-4 w-4" />
            Edit console
          </Link>
          <button
            type="button"
            onClick={handleDelete}
            disabled={deleting}
            className="flex items-center gap-2 rounded-lg border border-red-200 bg-white px-4 py-2.5 font-medium text-red-600 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {deleting ? (
              <LoaderCircle className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}

            {deleting
              ? 'Deleting...'
              : 'Delete'}
          </button>
        </div>
      </div>

      {/* Console image and structured information. */}
      <div className="mt-8 grid gap-6 lg:grid-cols-[minmax(0,1.3fr)_minmax(20rem,0.7fr)]">
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
          <div className="flex min-h-80 items-center justify-center bg-slate-100">
            {hasImage ? (
              <img
                src={imageUrl}
                alt={`${consoleItem.manufacturerName} ${consoleItem.consoleModelName}`}
                onError={() =>
                  setImageError(true)
                }
                className="max-h-128 w-full object-contain"
              />
            ) : (
              <div className="flex flex-col items-center gap-3 py-20 text-slate-400">
                <Gamepad2 className="h-20 w-20" />

                <span className="text-sm">
                  No image available
                </span>
              </div>
            )}
          </div>

          {imageError && (
            <div className="border-t border-amber-200 bg-amber-50 px-5 py-3 text-sm text-amber-700">
              The image URL exists, but the
              image could not be loaded.
            </div>
          )}
        </div>

        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="text-lg font-semibold text-slate-900">
            Console information
          </h2>
          <dl className="mt-6 space-y-5">
            <DetailRow
              icon={User}
              label="Owner"
              value={
                consoleItem.ownerName ||
                'Unknown'
              }
            />

            <DetailRow
              icon={MapPin}
              label="Region"
              value={
                consoleItem.region || '—'
              }
            />
            <DetailRow
              icon={Palette}
              label="Color"
              value={
                consoleItem.color || '—'
              }
            />
            <DetailRow
              icon={ShieldCheck}
              label="Condition"
              value={formatEnum(
                consoleItem.condition,
              )}
            />
            <DetailRow
              icon={Gamepad2}
              label="Status"
              value={formatEnum(
                consoleItem.status,
              )}
            />
            <DetailRow
              icon={CircleDollarSign}
              label="Estimated value"
              value={formatMoney(
                consoleItem.estimatedValue,
              )}
            />
          </dl>
        </div>
      </div>

      <div className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">
          Notes
        </h2>

        <p className="mt-4 whitespace-pre-wrap text-slate-600">
          {consoleItem.notes ||
            'No notes registered.'}
        </p>
      </div>

      <DeleteConfirmationModal
        open={deleteConfirmationOpen}
        title="Delete console?"
        message="This console, its associated repair cases and all component tests belonging to those repair cases will be permanently deleted. This action cannot be undone."
        deleting={deleting}
        onCancel={() =>
          setDeleteConfirmationOpen(false)
        }
        onConfirm={confirmDelete}
      />
    </section>
  )
}

// reusable presentation comp for console info displayed
const DetailRow = ({
  icon: Icon,
  label,
  value,
}) => (
  <div className="flex items-start gap-3">
    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
      <Icon className="h-4 w-4" />
    </div>

    <div className="min-w-0">
      <dt className="text-xs font-medium uppercase tracking-wide text-slate-400">
        {label}
      </dt>

      <dd className="mt-1 wrap-break-word font-medium text-slate-800">
        {value}
      </dd>
    </div>
  </div>
)

// small error component
const ErrorMessage = ({ message }) => (
  <div className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
    <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
    <span>{message}</span>
  </div>
)

const formatEnum = (value) => {
  if (!value) {
    return '—'
  }

  return value
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/\b\w/g, (character) =>
      character.toUpperCase(),
    )
}

// format the MoneyDTO
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
        currency: money.currency,
      },
    ).format(Number(money.amount))
  } catch {
    return `${money.amount} ${money.currency}`
  }
}

export default ConsoleDetails
