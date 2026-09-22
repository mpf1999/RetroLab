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
  Calendar,
  FlaskConical,
  Gamepad2,
  LoaderCircle,
  Pencil,
  Plus,
  Trash2,
} from 'lucide-react'

import {
  deleteRepairCase,
  getRepairCaseById,
  updateRepairCase,
} from '../api/repairCaseApi'

import {
  deleteComponentTest,
  getComponentTestsByRepairCaseId,
  updateComponentTest,
} from '../api/componentTestApi'

import DeleteConfirmationModal from '../components/DeleteConfirmationModal'
import FormField from '../components/FormField'
import ErrorMessage from '../components/ErrorMessage'
import getErrorMessage from '../utils/getErrorMessage'

const RepairCaseDetails = () => {
  const { repairCaseId } = useParams()
  const navigate = useNavigate()

  const [
    repairCase,
    setRepairCase,
  ] = useState(null)

  const [
    componentTests,
    setComponentTests,
  ] = useState([])

  const [editing, setEditing] =
    useState(false)

  const [formData, setFormData] =
    useState(null)

  const [loading, setLoading] =
    useState(true)

  const [saving, setSaving] =
    useState(false)

  const [
    deletingRepairCase,
    setDeletingRepairCase,
  ] = useState(false)

  const [
    deletingTestId,
    setDeletingTestId,
  ] = useState(null)

  const [
    editingTestId,
    setEditingTestId,
  ] = useState(null)

  const [
    testFormData,
    setTestFormData,
  ] = useState(null)

  const [
    savingTestId,
    setSavingTestId,
  ] = useState(null)

  const [error, setError] =
    useState('')

  const [deleteConfirmation, setDeleteConfirmation] =
    useState(null)

  // load repair case and component tests concurrently
  useEffect(() => {
    const loadRepairCaseDetails =
      async () => {
        try {
          setLoading(true)
          setError('')

          const [
            repairCaseData,
            componentTestsData,
          ] = await Promise.all([
            getRepairCaseById(
              repairCaseId,
            ),

            getComponentTestsByRepairCaseId(
              repairCaseId,
            ),
          ])

          setRepairCase(
            repairCaseData,
          )

          setFormData({
            consoleId:
              repairCaseData.consoleId,

            title:
              repairCaseData.title ?? '',

            description:
              repairCaseData.description ??
              '',

            status:
              repairCaseData.status ??
              'OPEN',
          })

          setComponentTests(
            Array.isArray(
              componentTestsData,
            )
              ? componentTestsData
              : [],
          )
        } catch (requestError) {
          setError(
            getErrorMessage(
              requestError,
              'Could not load the repair case.',
            ),
          )
        } finally {
          setLoading(false)
        }
      }

    loadRepairCaseDetails()
  }, [repairCaseId])

  // generic handler
  const handleChange = (event) => {
    const { name, value } =
      event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  // discard unsaved changes and restore the values currently stored in the loaded repair case

  const handleCancelEdit = () => {
    setFormData({
      consoleId:
        repairCase.consoleId,

      title:
        repairCase.title ?? '',

      description:
        repairCase.description ?? '',

      status:
        repairCase.status ?? 'OPEN',
    })

    setEditing(false)
    setError('')
  }

  // validate & persists
  const handleSave = async (event) => {
    event.preventDefault()

    if (!formData.title.trim()) {
      setError(
        'Repair case title is required.',
      )
      return
    }

    if (!formData.description.trim()) {
      setError(
        'Repair case description is required.',
      )

      return
    }

    try {
      setSaving(true)
      setError('')

      const updatedRepairCase =
        await updateRepairCase(
          repairCaseId,
          {
            consoleId:
              repairCase.consoleId,

            title:
              formData.title.trim(),

            description:
              formData.description.trim(),

            status:
              formData.status,
          },
        )

      // notify other components
      window.dispatchEvent(
        new CustomEvent(
          'retrolab:repair-cases-changed',
          {
            detail: updatedRepairCase,
          },
        ),
      )

      setRepairCase(
        updatedRepairCase,
      )

      setFormData({
        consoleId:
          updatedRepairCase.consoleId,

        title:
          updatedRepairCase.title,

        description:
          updatedRepairCase.description,

        status:
          updatedRepairCase.status,
      })

      setEditing(false)
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not update the repair case.',
        ),
      )
    } finally {
      setSaving(false)
    }
  }

  // open shared confirmation modal before delete

  const handleDeleteRepairCase = () => {
    setDeleteConfirmation({
      type: 'repairCase',
      title: 'Delete repair case?',
      message:
        componentTests.length > 0
          ? `This repair case and its ${componentTests.length} associated component ${componentTests.length === 1 ? 'test' : 'tests'} will be permanently deleted. This action cannot be undone.`
          : 'This repair case will be permanently deleted. This action cannot be undone.',
    })
  }

  // delete and return to list
  const confirmDeleteRepairCase = async () => {
    try {
      setDeletingRepairCase(true)
      setError('')
      await deleteRepairCase(repairCaseId)

      window.dispatchEvent(
        new CustomEvent(
          'retrolab:repair-cases-changed',
          {
            detail: {
              repairCaseId:
                Number(repairCaseId),
              deleted: true,
            },
          },
        ),
      )

      navigate(
        '/repair-cases',
        { replace: true },
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not delete the repair case.',
        ),
      )
    } finally {
      setDeletingRepairCase(false)
      setDeleteConfirmation(null)
    }
  }

  const handleEditComponentTest = (
    componentTest,
  ) => {
    setEditingTestId(
      componentTest.componentTestId,
    )

    setTestFormData({
      repairCaseId:
        componentTest.repairCaseId,

      componentId:
        componentTest.componentId,

      measuredVoltage:
        componentTest.measuredVoltage ?? '',

      measuredCurrent:
        componentTest.measuredCurrent ?? '',

      measuredResistance:
        componentTest.measuredResistance ?? '',

      temperature:
        componentTest.temperature ?? '',

      continuity:
        componentTest.continuity === null ||
        componentTest.continuity === undefined
          ? ''
          : String(
              componentTest.continuity,
            ),

      result:
        componentTest.result ??
        'NOT_TESTED',

      notes:
        componentTest.notes ?? '',
    })

    setError('')
  }

  const handleTestChange = (event) => {
    const { name, value } =
      event.target

    setTestFormData(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }

  const handleCancelTestEdit = () => {
    setEditingTestId(null)
    setTestFormData(null)
    setError('')
  }

  const handleSaveComponentTest =
    async (
      event,
      componentTestId,
    ) => {
      event.preventDefault()

      const toNullableNumber =
        (value) =>
          value === ''
            ? null
            : Number(value)

      try {
        setSavingTestId(
          componentTestId,
        )
        setError('')

        const updatedComponentTest =
          await updateComponentTest(
            componentTestId,
            {
              repairCaseId:
                testFormData.repairCaseId,

              componentId:
                testFormData.componentId,

              measuredVoltage:
                toNullableNumber(
                  testFormData.measuredVoltage,
                ),

              measuredCurrent:
                toNullableNumber(
                  testFormData.measuredCurrent,
                ),

              measuredResistance:
                toNullableNumber(
                  testFormData.measuredResistance,
                ),

              temperature:
                toNullableNumber(
                  testFormData.temperature,
                ),

              continuity:
                testFormData.continuity ===
                ''
                  ? null
                  : testFormData.continuity ===
                    'true',

              result:
                testFormData.result,

              notes:
                testFormData.notes.trim() ||
                null,
            },
          )

        setComponentTests(
          (currentTests) =>
            currentTests.map(
              (componentTest) =>
                componentTest.componentTestId ===
                componentTestId
                  ? updatedComponentTest
                  : componentTest,
            ),
        )

        setEditingTestId(null)
        setTestFormData(null)
      } catch (requestError) {
        setError(
          getErrorMessage(
            requestError,
            'Could not update the component test.',
          ),
        )
      } finally {
        setSavingTestId(null)
      }
    }

  // ask for confirmation when deleting an individual comp test
  const handleDeleteComponentTest = (
    componentTestId,
  ) => {
    setDeleteConfirmation({
      type: 'componentTest',
      componentTestId,
      title: 'Delete component test?',
      message:
        'This component test will be permanently deleted. This action cannot be undone.',
    })
  }

  // delete comp test without having to reload repair case
  const confirmDeleteComponentTest =
    async () => {
      const componentTestId =
        deleteConfirmation.componentTestId

      try {
        setDeletingTestId(
          componentTestId,
        )
        setError('')

        await deleteComponentTest(
          componentTestId,
        )

        setComponentTests(
          (currentTests) =>
            currentTests.filter(
              (componentTest) =>
                componentTest.componentTestId !==
                componentTestId,
            ),
        )
      } catch (requestError) {
        setError(
          getErrorMessage(
            requestError,
            'Could not delete the component test.',
          ),
        )
      } finally {
        setDeletingTestId(null)
        setDeleteConfirmation(null)
      }
    }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center">
        <LoaderCircle className="h-9 w-9 animate-spin text-primary-500" />
      </div>
    )
  }

  if (!repairCase) {
    return (
      <section className="mx-auto max-w-6xl">
        <ErrorMessage
          message={
            error ||
            'Repair case not found.'
          }
        />

        <Link
          to="/repair-cases"
          className="mt-6 inline-flex items-center gap-2 text-sm font-medium text-primary-600"
        >
          <ArrowLeft className="h-4 w-4" />

          Back to repair cases
        </Link>
      </section>
    )
  }

  return (
    <section className="mx-auto max-w-6xl">
      <Link
        to="/repair-cases"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition-colors hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />

        Back to repair cases
      </Link>

      {error && (
        <div className="mt-6">
          <ErrorMessage
            message={error}
          />
        </div>
      )}

      <div className="mt-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-3xl font-bold text-slate-900">
              {repairCase.title}
            </h1>

            <StatusBadge
              status={
                repairCase.status
              }
            />
          </div>

          <p className="mt-2 text-slate-500">
            Repair case #
            {repairCase.repairCaseId}
          </p>
        </div>

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            onClick={() =>
              setEditing(true)
            }
            disabled={
              editing ||
              deletingRepairCase
            }
            className="flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Pencil className="h-4 w-4" />

            Edit
          </button>

          <button
            type="button"
            onClick={
              handleDeleteRepairCase
            }
            disabled={
              deletingRepairCase
            }
            className="flex items-center gap-2 rounded-lg border border-red-200 bg-white px-4 py-2.5 font-medium text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {deletingRepairCase ? (
              <LoaderCircle className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}

            {deletingRepairCase
              ? 'Deleting...'
              : 'Delete case'}
          </button>
        </div>
      </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-3">
        <div className="space-y-6 lg:col-span-2">
          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold text-slate-900">
              Repair information
            </h2>

            {editing ? (
              <form
                onSubmit={handleSave}
                className="mt-6 space-y-5"
              >
                <FormField
                  label="Title"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  required
                  maxLength={255}
                  disabled={saving}
                />

                <FormField
                  label="Description"
                  name="description"
                  value={
                    formData.description
                  }
                  onChange={handleChange}
                  required
                  textarea
                  rows={5}
                  maxLength={2000}
                  disabled={saving}
                />

                <FormField
                  label="Status"
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                  disabled={saving}
                  required
                >
                  <option value="OPEN">
                    Open
                  </option>

                  <option value="IN_PROGRESS">
                    In progress
                  </option>

                  <option value="CLOSED">
                    Closed
                  </option>
                </FormField>

                <div className="flex justify-end gap-3 border-t border-slate-200 pt-5">
                  <button
                    type="button"
                    onClick={
                      handleCancelEdit
                    }
                    disabled={saving}
                    className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50"
                  >
                    Cancel
                  </button>

                  <button
                    type="submit"
                    disabled={saving}
                    className="flex items-center gap-2 rounded-lg bg-primary-500 px-4 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:opacity-50"
                  >
                    {saving && (
                      <LoaderCircle className="h-4 w-4 animate-spin" />
                    )}

                    {saving
                      ? 'Saving...'
                      : 'Save changes'}
                  </button>
                </div>
              </form>
            ) : (
              <p className="mt-4 whitespace-pre-wrap text-slate-600">
                {
                  repairCase.description
                }
              </p>
            )}
          </div>

          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
              <div>
                <h2 className="text-lg font-semibold text-slate-900">
                  Component tests
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  {componentTests.length}{' '}
                  {componentTests.length ===
                  1
                    ? 'test registered'
                    : 'tests registered'}
                </p>
              </div>

              <Link
                to={`/component-tests/new?repairCaseId=${repairCase.repairCaseId}`}
                className="flex items-center justify-center gap-2 rounded-lg bg-emerald-500 px-4 py-2.5 font-semibold text-white hover:bg-emerald-600"
              >
                <Plus className="h-4 w-4" />

                Add test
              </Link>
            </div>

            {componentTests.length ===
            0 ? (
              <div className="mt-6 rounded-lg bg-slate-50 p-8 text-center">
                <FlaskConical className="mx-auto h-10 w-10 text-slate-300" />

                <p className="mt-3 text-sm text-slate-500">
                  No component tests have
                  been registered.
                </p>
              </div>
            ) : (
              <div className="mt-6 space-y-4">
                {componentTests.map(
                  (componentTest) => (
                    <ComponentTestCard
                      key={
                        componentTest.componentTestId
                      }
                      componentTest={
                        componentTest
                      }
                      deleting={
                        deletingTestId ===
                        componentTest.componentTestId
                      }
                      editing={
                        editingTestId ===
                        componentTest.componentTestId
                      }
                      saving={
                        savingTestId ===
                        componentTest.componentTestId
                      }
                      formData={
                        editingTestId ===
                        componentTest.componentTestId
                          ? testFormData
                          : null
                      }
                      onEdit={() =>
                        handleEditComponentTest(
                          componentTest,
                        )
                      }
                      onChange={
                        handleTestChange
                      }
                      onCancel={
                        handleCancelTestEdit
                      }
                      onSave={(event) =>
                        handleSaveComponentTest(
                          event,
                          componentTest.componentTestId,
                        )
                      }
                      onDelete={() =>
                        handleDeleteComponentTest(
                          componentTest.componentTestId,
                        )
                      }
                    />
                  ),
                )}
              </div>
            )}
          </div>
        </div>

        <aside className="space-y-6">
          <InfoCard
            icon={Gamepad2}
            label="Console"
            value={
              repairCase.consoleName
            }
          />

          <InfoCard
            icon={Calendar}
            label="Start date"
            value={formatDate(
              repairCase.startDate,
            )}
          />

          <InfoCard
            icon={Calendar}
            label="End date"
            value={formatDate(
              repairCase.endDate,
            )}
          />
        </aside>
      </div>

      <DeleteConfirmationModal
        open={
          deleteConfirmation !== null
        }
        title={
          deleteConfirmation?.title
        }
        message={
          deleteConfirmation?.message
        }
        deleting={
          deleteConfirmation?.type ===
          'repairCase'
            ? deletingRepairCase
            : deletingTestId !== null
        }
        onCancel={() =>
          setDeleteConfirmation(null)
        }
        onConfirm={
          deleteConfirmation?.type ===
          'repairCase'
            ? confirmDeleteRepairCase
            : confirmDeleteComponentTest
        }
      />
    </section>
  )
}

// present the result
const ComponentTestCard = ({
  componentTest,
  deleting,
  editing,
  saving,
  formData,
  onEdit,
  onChange,
  onCancel,
  onSave,
  onDelete,
}) => (
  <article className="rounded-lg border border-slate-200 p-4">
    <div className="flex items-start justify-between gap-4">
      <div className="min-w-0">
        <h3 className="truncate font-semibold text-slate-900">
          {componentTest.componentName}
        </h3>

        <p className="mt-1 text-xs text-slate-500">
          {formatDate(
            componentTest.testDate,
          )}
        </p>
      </div>

      <div className="flex shrink-0 items-center gap-2">
        {!editing && (
          <ResultBadge
            result={
              componentTest.result
            }
          />
        )}

        {!editing && (
          <button
            type="button"
            onClick={onEdit}
            disabled={deleting}
            aria-label={`Edit test for ${componentTest.componentName}`}
            title="Edit component test"
            className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-slate-100 hover:text-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Pencil className="h-4 w-4" />
          </button>
        )}

        {!editing && (
          <button
            type="button"
            onClick={onDelete}
            disabled={deleting}
            aria-label={`Delete test for ${componentTest.componentName}`}
            title="Delete component test"
            className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-400 transition-colors hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {deleting ? (
              <LoaderCircle className="h-4 w-4 animate-spin" />
            ) : (
              <Trash2 className="h-4 w-4" />
            )}
          </button>
        )}
      </div>
    </div>

    {editing ? (
      <form
        onSubmit={onSave}
        className="mt-4 space-y-4"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <FormField
            label="Voltage (V)"
            name="measuredVoltage"
            type="number"
            min="0"
            step="0.01"
            value={
              formData.measuredVoltage
            }
            onChange={onChange}
            disabled={saving}
          />

          <FormField
            label="Current (A)"
            name="measuredCurrent"
            type="number"
            min="0"
            step="0.01"
            value={
              formData.measuredCurrent
            }
            onChange={onChange}
            disabled={saving}
          />

          <FormField
            label="Resistance (Ω)"
            name="measuredResistance"
            type="number"
            min="0"
            step="0.01"
            value={
              formData.measuredResistance
            }
            onChange={onChange}
            disabled={saving}
          />

          <FormField
            label="Temperature (°C)"
            name="temperature"
            type="number"
            step="0.01"
            value={
              formData.temperature
            }
            onChange={onChange}
            disabled={saving}
          />

          <FormField
            label="Continuity"
            name="continuity"
            value={
              formData.continuity
            }
            onChange={onChange}
            disabled={saving}
          >
            <option value="">
              Not measured
            </option>

            <option value="true">
              Yes
            </option>

            <option value="false">
              No
            </option>
          </FormField>

          <FormField
            label="Result"
            name="result"
            value={formData.result}
            onChange={onChange}
            disabled={saving}
            required
          >
            <option value="NOT_TESTED">
              Not tested
            </option>

            <option value="PASS">
              Pass
            </option>

            <option value="WARNING">
              Warning
            </option>

            <option value="FAIL">
              Fail
            </option>
          </FormField>
        </div>

        <FormField
          label="Notes"
          name="notes"
          value={formData.notes}
          onChange={onChange}
          textarea
          rows={4}
          maxLength={2000}
          disabled={saving}
        />

        <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
          <button
            type="button"
            onClick={onCancel}
            disabled={saving}
            className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50"
          >
            Cancel
          </button>

          <button
            type="submit"
            disabled={saving}
            className="flex items-center gap-2 rounded-lg bg-primary-500 px-4 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:opacity-50"
          >
            {saving && (
              <LoaderCircle className="h-4 w-4 animate-spin" />
            )}

            {saving
              ? 'Saving...'
              : 'Save changes'}
          </button>
        </div>
      </form>
    ) : (
      <>
        <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-5">
          <Measurement
            label="Voltage"
            value={
              componentTest.measuredVoltage
            }
            unit="V"
          />

          <Measurement
            label="Current"
            value={
              componentTest.measuredCurrent
            }
            unit="A"
          />

          <Measurement
            label="Resistance"
            value={
              componentTest.measuredResistance
            }
            unit="Ω"
          />

          <Measurement
            label="Temperature"
            value={
              componentTest.temperature
            }
            unit="°C"
          />

          <Measurement
            label="Continuity"
            value={
              componentTest.continuity ===
                null ||
              componentTest.continuity ===
                undefined
                ? null
                : componentTest.continuity
                  ? 'Yes'
                  : 'No'
            }
            unit=""
          />
        </div>

        {componentTest.notes && (
          <div className="mt-4 border-t border-slate-100 pt-4">
            <p className="whitespace-pre-wrap text-sm text-slate-600">
              {componentTest.notes}
            </p>
          </div>
        )}
      </>
    )}
  </article>
)

const Measurement = ({
  label,
  value,
  unit,
}) => (
  <div className="rounded-lg bg-slate-50 p-3">
    <p className="text-xs text-slate-500">
      {label}
    </p>

    <p className="mt-1 font-mono font-semibold text-slate-800">
      {value === null ||
      value === undefined
        ? '—'
        : unit
          ? `${value} ${unit}`
          : value}
    </p>
  </div>
)

const InfoCard = ({
  icon: Icon,
  label,
  value,
}) => (
  <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
    <div className="flex items-center gap-3">
      <Icon className="h-5 w-5 shrink-0 text-primary-500" />

      <div className="min-w-0">
        <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
          {label}
        </p>

        <p className="mt-1 wrap-break-word font-medium text-slate-800">
          {value || '—'}
        </p>
      </div>
    </div>
  </div>
)

const StatusBadge = ({ status }) => {
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
      {formatEnum(status)}
    </span>
  )
}

const ResultBadge = ({ result }) => {
  const styles = {
    PASS:
      'bg-emerald-100 text-emerald-700',

    FAIL:
      'bg-red-100 text-red-700',

    WARNING:
      'bg-amber-100 text-amber-700',

    NOT_TESTED:
      'bg-slate-100 text-slate-600',
  }

  const normalizedResult =
    result ?? 'NOT_TESTED'

  return (
    <span
      className={`rounded-full px-3 py-1 text-xs font-semibold ${
        styles[normalizedResult] ??
        styles.NOT_TESTED
      }`}
    >
      {formatEnum(
        normalizedResult,
      )}
    </span>
  )
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

const formatDate = (date) => {
  if (!date) {
    return '—'
  }

  return new Intl.DateTimeFormat(
    'en-GB',
    {
      dateStyle: 'medium',
      timeStyle: 'short',
    },
  ).format(new Date(date))
}

export default RepairCaseDetails