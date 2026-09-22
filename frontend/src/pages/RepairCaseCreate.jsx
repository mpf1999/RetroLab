import {
  useEffect,
  useState,
} from 'react'

import {
  useNavigate,
} from 'react-router-dom'

import {
  ArrowLeft,
  LoaderCircle,
  Save,
  Wrench,
} from 'lucide-react'

import {
  createRepairCase,
} from '../api/repairCaseApi'

import {
  getConsoles,
} from '../api/consoleApi'

import FormField
  from '../components/FormField'

import getErrorMessage
  from '../utils/getErrorMessage'


const RepairCaseCreate = () => {
  const navigate =
    useNavigate()

  const [
    consoles,
    setConsoles,
  ] = useState([])

  const [
    formData,
    setFormData,
  ] = useState({
    consoleId: '',
    title: '',
    description: '',
  })

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    submitting,
    setSubmitting,
  ] = useState(false)

  const [
    error,
    setError,
  ] = useState('')


  // loads consoles availabkle
  useEffect(() => {
    const loadConsoles =
      async () => {
        try {
          setLoading(true)
          setError('')

          const consolesData =
            await getConsoles()

          setConsoles(
            Array.isArray(
              consolesData,
            )
              ? consolesData
              : [],
          )
        } catch (requestError) {
          setError(
            getErrorMessage(
              requestError,
              'Could not load consoles.',
            ),
          )
        } finally {
          setLoading(false)
        }
      }

    loadConsoles()
  }, [])


  // updates field in formData
  const handleInputChange = (
    event,
  ) => {
    const {
      name,
      value,
    } = event.target
    setFormData(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }


  // validates form before sending to backend
  const validateForm = () => {
    if (!formData.consoleId) {
      setError(
        'Select a console.',
      )

      return false
    }

    if (
      !formData.title.trim()
    ) {
      setError(
        'Repair case title is required.',
      )

      return false
    }

    if (
      !formData.description.trim()
    ) {
      setError(
        'Repair case description is required.',
      )
      return false
    }

    return true
  }

  const handleSubmit = async (
    event,
  ) => {
    event.preventDefault()

    setError('')

    if (!validateForm()) {
      return
    }

    try {
      setSubmitting(true)

      // new repair case is always open
      const createdRepairCase =
        await createRepairCase({
          consoleId: Number(
            formData.consoleId,
          ),

          title:
            formData.title.trim(),

          description:
            formData.description.trim(),

          status: 'OPEN',
        })


      // notify other components
      window.dispatchEvent(
        new CustomEvent(
          'retrolab:repair-cases-changed',
        ),
      )
      // open the repair case
      navigate(
        `/repair-cases/${createdRepairCase.repairCaseId}`,
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not create the repair case.',
        ),
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-96 items-center justify-center">
        <LoaderCircle className="h-9 w-9 animate-spin text-primary-500" />
      </div>
    )
  }


  return (
    <section className="mx-auto max-w-3xl">
      <button
        type="button"
        onClick={() =>
          navigate(
            '/repair-cases',
          )
        }
        className="mb-6 flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-slate-900"
      >
        <ArrowLeft className="h-4 w-4" />

        <span>
          Back to repair cases
        </span>
      </button>

      <div className="flex items-center gap-3">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
          <Wrench className="h-5 w-5" />
        </div>

        <div>
          <h1 className="text-3xl font-bold text-slate-900">
            New Repair Case
          </h1>

          <p className="mt-1 text-slate-500">
            Create a repair case for a registered console.
          </p>
        </div>
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
        className="mt-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
      >

        {/* Console */}
        <FormField
          label="Console"
          name="consoleId"
          value={
            formData.consoleId
          }
          onChange={
            handleInputChange
          }
          disabled={
            submitting
          }
          required
        >
          <option value="">
            Select a console
          </option>

          {consoles.map(
            (consoleItem) => (
              <option
                key={
                  consoleItem.consoleId
                }
                value={
                  consoleItem.consoleId
                }
              >
                {
                  consoleItem.manufacturerName
                }{' '}
                {
                  consoleItem.consoleModelName
                }
                {' — '}
                {
                  consoleItem.serialNumber
                }
              </option>
            ),
          )}
        </FormField>


        {consoles.length === 0 && (
          <p className="mt-2 text-sm text-amber-600">
            There are no registered
            consoles available.
          </p>
        )}

        <div className="mt-6">
          <FormField
            label="Title"
            name="title"
            value={
              formData.title
            }
            onChange={
              handleInputChange
            }
            disabled={
              submitting
            }
            maxLength={255}
            placeholder="Example: Console does not power on"
            required
          />
        </div>

        <div className="mt-6">
          <FormField
            label="Description"
            name="description"
            value={
              formData.description
            }
            onChange={
              handleInputChange
            }
            disabled={
              submitting
            }
            textarea
            rows={5}
            maxLength={2000}
            placeholder="Describe the problem and the initial condition of the console..."
            required
          />
        </div>

        <div className="mt-6">
          <p className="mb-2 text-sm font-medium text-slate-700">
            Initial status
          </p>

          <div className="rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm font-medium text-blue-700">
            Open
          </div>
        </div>

        <div className="mt-8 flex items-center justify-between border-t border-slate-200 pt-6">

          <button
            type="button"
            onClick={() =>
              navigate(
                '/repair-cases',
              )
            }
            disabled={
              submitting
            }
            className="flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50"
          >
            <ArrowLeft className="h-4 w-4" />

            Cancel
          </button>


          <button
            type="submit"
            disabled={
              submitting ||
              consoles.length === 0
            }
            className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {submitting ? (
              <LoaderCircle className="h-4 w-4 animate-spin" />
            ) : (
              <Save className="h-4 w-4" />
            )}

            {submitting
              ? 'Creating...'
              : 'Create Repair Case'}
          </button>
        </div>
      </form>
    </section>
  )
}


export default RepairCaseCreate