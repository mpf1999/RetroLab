import {
  useEffect,
  useMemo,
  useState,
} from 'react'

import {
  useNavigate,
  useSearchParams,
} from 'react-router-dom'

import {
  Activity,
  ArrowLeft,
  ArrowRight,
  Check,
  Cpu,
  FlaskConical,
  Gauge,
  LoaderCircle,
  Save,
  Thermometer,
  Wrench,
  Zap,
} from 'lucide-react'

import {
  createRepairCase,
  getRepairCases,
} from '../api/repairCaseApi'

import {
  getConsoleById,
  getConsoles,
} from '../api/consoleApi'

import {
  createComponent,
  getComponentsByConsoleModelId,
} from '../api/componentApi'

import {
  createComponentTest,
} from '../api/componentTestApi'

import FormField
  from '../components/FormField'


const emptyMeasurements = {
  measuredVoltage: '',
  measuredCurrent: '',
  measuredResistance: '',
  temperature: '',
  result: 'NOT_TESTED',
  notes: '',
}


const ComponentTestCreate = () => {
  const navigate = useNavigate()

  const [searchParams] =
    useSearchParams()

  const requestedRepairCaseId =
    searchParams.get('repairCaseId')

  const [
    currentStep,
    setCurrentStep,
  ] = useState(1)

  const [
    error,
    setError,
  ] = useState('')

  const [
    loading,
    setLoading,
  ] = useState(true)

  const [
    loadingComponents,
    setLoadingComponents,
  ] = useState(false)

  const [
    submitting,
    setSubmitting,
  ] = useState(false)

  const [
    repairCases,
    setRepairCases,
  ] = useState([])

  const [
    consoles,
    setConsoles,
  ] = useState([])

  const [
    availableComponents,
    setAvailableComponents,
  ] = useState([])

  const [
    selectedConsole,
    setSelectedConsole,
  ] = useState(null)

  const [
    repairCaseMode,
    setRepairCaseMode,
  ] = useState('existing')

  const [
    selectedRepairCaseId,
    setSelectedRepairCaseId,
  ] = useState('')

  const [
    newRepairCase,
    setNewRepairCase,
  ] = useState({
    consoleId: '',
    title: '',
    description: '',
  })

  const [
    componentMode,
    setComponentMode,
  ] = useState('existing')

  const [
    selectedComponentId,
    setSelectedComponentId,
  ] = useState('')

  const [
    newComponent,
    setNewComponent,
  ] = useState({
    name: '',
    description: '',
  })

  const [
    measurements,
    setMeasurements,
  ] = useState(
    emptyMeasurements,
  )


  // load the data required
  useEffect(() => {
    const loadInitialData =
      async () => {
        try {
          setLoading(true)
          setError('')

          const [
            repairCasesData,
            consolesData,
          ] = await Promise.all([
            getRepairCases(),
            getConsoles(),
          ])

          // closed repair cases cannot receive new tests
          const activeRepairCases =
            Array.isArray(
              repairCasesData,
            )
              ? repairCasesData.filter(
                  (repairCase) =>
                    repairCase.status !==
                    'CLOSED',
                )
              : []

          setRepairCases(
            activeRepairCases,
          )

          setConsoles(
            Array.isArray(
              consolesData,
            )
              ? consolesData
              : [],
          )

          if (requestedRepairCaseId) {
            const requestedRepairCase =
              activeRepairCases.find(
                (repairCase) =>
                  String(
                    repairCase.repairCaseId,
                  ) ===
                  String(
                    requestedRepairCaseId,
                  ),
              )

            if (
              requestedRepairCase
            ) {
              setSelectedRepairCaseId(
                String(
                  requestedRepairCase
                    .repairCaseId,
                ),
              )
            }
          }
        } catch (requestError) {
          setError(
            getErrorMessage(
              requestError,
              'Could not load the required data.',
            ),
          )
        } finally {
          setLoading(false)
        }
      }

    loadInitialData()
  }, [requestedRepairCaseId])


  // find complete repair case object
  const selectedRepairCase =
    useMemo(() => {
      return repairCases.find(
        (repairCase) =>
          String(
            repairCase.repairCaseId,
          ) ===
          String(
            selectedRepairCaseId,
          ),
      )
    }, [
      repairCases,
      selectedRepairCaseId,
    ])


  // update form used to create a new repair case
  const handleRepairCaseInputChange = (
    event,
  ) => {
    const {
      name,
      value,
    } = event.target

    setNewRepairCase(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }


  // update form to create a new component
  const handleComponentInputChange = (
    event,
  ) => {
    const {
      name,
      value,
    } = event.target

    setNewComponent(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }


  // update component test measurement values
  const handleMeasurementChange = (
    event,
  ) => {
    const {
      name,
      value,
    } = event.target

    setMeasurements(
      (currentData) => ({
        ...currentData,
        [name]: value,
      }),
    )
  }


  // switch between selecting an existing repair case or creating one
  const changeRepairCaseMode = (
    mode,
  ) => {
    setRepairCaseMode(mode)

    setError('')

    setAvailableComponents([])

    setSelectedComponentId('')
    setSelectedConsole(null)
  }

  // same with component
  const changeComponentMode = (
    mode,
  ) => {
    setComponentMode(mode)

    setSelectedComponentId('')

    setError('')
  }


  // rep case --> console ---> console model --> comps
  const goToComponentStep =
    async () => {
      setError('')

      let consoleData
      if (
        repairCaseMode ===
        'existing'
      ) {
        if (
          !selectedRepairCase
        ) {
          setError(
            'Select a repair case.',
          )

          return
        }
        try {
          setLoadingComponents(
            true,
          )

          consoleData =
            await getConsoleById(
              selectedRepairCase
                .consoleId,
            )
        } catch (
          requestError
        ) {
          setError(
            getErrorMessage(
              requestError,
              'Could not load the console.',
            ),
          )

          setLoadingComponents(
            false,
          )

          return
        }
      } else {
        if (
          !newRepairCase.consoleId
        ) {
          setError(
            'Select a console.',
          )

          return
        }

        if (
          !newRepairCase.title.trim()
        ) {
          setError(
            'Repair case title is required.',
          )

          return
        }

        if (
          !newRepairCase
            .description
            .trim()
        ) {
          setError(
            'Repair case description is required.',
          )

          return
        }

        consoleData =
          consoles.find(
            (consoleItem) =>
              String(
                consoleItem.consoleId,
              ) ===
              String(
                newRepairCase.consoleId,
              ),
          )

        if (!consoleData) {
          setError(
            'The selected console was not found.',
          )

          return
        }

        setLoadingComponents(
          true,
        )
      }
      // load components
      try {
        const componentsData =
          await getComponentsByConsoleModelId(
            consoleData
              .consoleModelId,
          )

        setSelectedConsole(
          consoleData,
        )

        setAvailableComponents(
          Array.isArray(
            componentsData,
          )
            ? componentsData
            : [],
        )

        setSelectedComponentId(
          '',
        )

        setCurrentStep(2)
      } catch (requestError) {
        setError(
          getErrorMessage(
            requestError,
            'Could not load the components.',
          ),
        )
      } finally {
        setLoadingComponents(
          false,
        )
      }
    }


  // validate component step before moving to measurements
  const goToMeasurementsStep =
    () => {
      setError('')
      if (
        componentMode ===
          'existing' &&
        !selectedComponentId
      ) {
        setError(
          'Select a component.',
        )

        return
      }

      if (
        componentMode === 'new' &&
        !newComponent.name.trim()
      ) {
        setError(
          'Component name is required.',
        )

        return
      }

      setCurrentStep(3)
    }


  // create all resorces and finally comp test
  const handleSubmit = async (
    event,
  ) => {
    event.preventDefault()

    try {
      setSubmitting(true)

      setError('')
      let repairCase =
        selectedRepairCase

      if (
        repairCaseMode === 'new'
      ) {
        repairCase =
          await createRepairCase({
            consoleId: Number(
              newRepairCase.consoleId,
            ),

            title:
              newRepairCase.title,

            description:
              newRepairCase
                .description,

            status: 'OPEN',
          })
      }

      if (!repairCase) {
        throw new Error(
          'The repair case could not be determined.',
        )
      }
      let componentId

      if (
        componentMode ===
        'existing'
      ) {
        componentId = Number(
          selectedComponentId,
        )
      } else {
        const createdComponent =
          await createComponent({
            consoleModelId:
              Number(
                selectedConsole
                  .consoleModelId,
              ),

            name:
              newComponent
                .name
                .trim(),

            description:
              newComponent
                .description
                .trim(),
          })

        componentId =
          createdComponent
            .componentId
      }

      await createComponentTest({
        repairCaseId:
          repairCase
            .repairCaseId,

        componentId,

        measuredVoltage:
          measurements
            .measuredVoltage,

        measuredCurrent:
          measurements
            .measuredCurrent,

        measuredResistance:
          measurements
            .measuredResistance,

        temperature:
          measurements
            .temperature,

        result:
          measurements.result,

        notes:
          measurements.notes,
      })


      // notify other components
      if (
        repairCaseMode === 'new'
      ) {
        window.dispatchEvent(
          new CustomEvent(
            'retrolab:repair-cases-changed',
          ),
        )
      }


      // open repair case after creation
      navigate(
        `/repair-cases/${repairCase.repairCaseId}`,
      )
    } catch (requestError) {
      setError(
        getErrorMessage(
          requestError,
          'Could not create the component test.',
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
    <section className="mx-auto max-w-5xl">
      <div>
        <h1 className="text-3xl font-bold text-slate-900">
          Run Component Test
        </h1>

        <p className="mt-2 text-slate-500">
          Select a repair case and component before
          recording the measurements.
        </p>
      </div>


      <StepIndicator
        currentStep={
          currentStep
        }
      />


      {error && (
        <div className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {error}
        </div>
      )}


      {currentStep === 1 && (
        <RepairCaseStep
          repairCaseMode={
            repairCaseMode
          }

          setRepairCaseMode={
            changeRepairCaseMode
          }

          repairCases={
            repairCases
          }

          consoles={
            consoles
          }

          selectedRepairCaseId={
            selectedRepairCaseId
          }

          setSelectedRepairCaseId={
            setSelectedRepairCaseId
          }

          newRepairCase={
            newRepairCase
          }

          handleRepairCaseChange={
            handleRepairCaseInputChange
          }

          loadingComponents={
            loadingComponents
          }

          onContinue={
            goToComponentStep
          }
        />
      )}


      {currentStep === 2 && (
        <ComponentStep
          selectedConsole={
            selectedConsole
          }

          componentMode={
            componentMode
          }

          setComponentMode={
            changeComponentMode
          }

          availableComponents={
            availableComponents
          }

          selectedComponentId={
            selectedComponentId
          }

          setSelectedComponentId={
            setSelectedComponentId
          }

          newComponent={
            newComponent
          }

          handleComponentChange={
            handleComponentInputChange
          }

          onBack={() => {
            setError('')
            setCurrentStep(1)
          }}

          onContinue={
            goToMeasurementsStep
          }
        />
      )}


      {currentStep === 3 && (
        <MeasurementsStep
          measurements={
            measurements
          }

          handleMeasurementChange={
            handleMeasurementChange
          }

          submitting={
            submitting
          }

          onBack={() => {
            setError('')
            setCurrentStep(2)
          }}

          onSubmit={
            handleSubmit
          }
        />
      )}
    </section>
  )
}


// display the three steps
const StepIndicator = ({
  currentStep,
}) => {
  const steps = [
    {
      number: 1,
      label: 'Repair case',
      icon: Wrench,
    },
    {
      number: 2,
      label: 'Component',
      icon: Cpu,
    },
    {
      number: 3,
      label: 'Measurements',
      icon: FlaskConical,
    },
  ]

  return (
    <div className="mt-8 grid gap-3 sm:grid-cols-3">
      {steps.map((step) => {
        const Icon =
          step.icon

        const completed =
          currentStep >
          step.number

        const active =
          currentStep ===
          step.number

        return (
          <div
            key={
              step.number
            }

            className={`flex items-center gap-3 rounded-xl border p-4 ${
              active
                ? 'border-primary-500 bg-primary-50'
                : completed
                  ? 'border-emerald-200 bg-emerald-50'
                  : 'border-slate-200 bg-white'
            }`}
          >
            <div
              className={`flex h-10 w-10 items-center justify-center rounded-full ${
                active
                  ? 'bg-primary-500 text-white'
                  : completed
                    ? 'bg-emerald-500 text-white'
                    : 'bg-slate-100 text-slate-500'
              }`}
            >
              {completed ? (
                <Check className="h-5 w-5" />
              ) : (
                <Icon className="h-5 w-5" />
              )}
            </div>


            <div>
              <p className="text-xs text-slate-500">
                Step{' '}
                {step.number}
              </p>

              <p className="font-semibold text-slate-800">
                {step.label}
              </p>
            </div>
          </div>
        )
      })}
    </div>
  )
}

// reusable selector

const SelectionMode = ({
  value,
  onChange,
  existingLabel,
  createLabel,
}) => (
  <div className="grid gap-3 sm:grid-cols-2">
    <button
      type="button"

      onClick={() =>
        onChange(
          'existing',
        )
      }

      className={`rounded-lg border px-4 py-3 text-left font-medium ${
        value === 'existing'
          ? 'border-primary-500 bg-primary-50 text-primary-700'
          : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
      }`}
    >
      {existingLabel}
    </button>


    <button
      type="button"

      onClick={() =>
        onChange('new')
      }

      className={`rounded-lg border px-4 py-3 text-left font-medium ${
        value === 'new'
          ? 'border-primary-500 bg-primary-50 text-primary-700'
          : 'border-slate-300 bg-white text-slate-700 hover:bg-slate-50'
      }`}
    >
      {createLabel}
    </button>
  </div>
)


// STEP 1
const RepairCaseStep = ({
  repairCaseMode,
  setRepairCaseMode,
  repairCases,
  consoles,
  selectedRepairCaseId,
  setSelectedRepairCaseId,
  newRepairCase,
  handleRepairCaseChange,
  loadingComponents,
  onContinue,
}) => (
  <div className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
    <h2 className="text-xl font-semibold text-slate-900">
      Select Repair Case
    </h2>

    <p className="mt-1 text-sm text-slate-500">
      Choose an existing repair case or create a new
      one.
    </p>


    <div className="mt-6">
      <SelectionMode
        value={
          repairCaseMode
        }

        onChange={
          setRepairCaseMode
        }

        existingLabel="Select existing repair case"

        createLabel="Create new repair case"
      />
    </div>


    {repairCaseMode ===
    'existing' ? (
      <div className="mt-6">
        <FormField
          label="Repair case"
          name="repairCaseId"
          value={
            selectedRepairCaseId
          }

          onChange={(
            event,
          ) =>
            setSelectedRepairCaseId(
              event.target
                .value,
            )
          }
        >
          <option value="">
            Select a repair case
          </option>

          {repairCases.map(
            (repairCase) => (
              <option
                key={
                  repairCase
                    .repairCaseId
                }

                value={
                  repairCase
                    .repairCaseId
                }
              >
                #
                {
                  repairCase
                    .repairCaseId
                }{' '}
                —{' '}
                {
                  repairCase.title
                }{' '}
                —{' '}
                {
                  repairCase
                    .consoleName
                }{' '}
                (
                {formatStatus(
                  repairCase
                    .status,
                )}
                )
              </option>
            ),
          )}
        </FormField>


        {repairCases.length ===
          0 && (
          <p className="mt-2 text-sm text-amber-600">
            There are no active repair cases.
          </p>
        )}
      </div>
    ) : (
      <div className="mt-6 grid gap-6 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <FormField
            label="Console"
            name="consoleId"

            value={
              newRepairCase
                .consoleId
            }

            onChange={
              handleRepairCaseChange
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
                    consoleItem
                      .consoleId
                  }

                  value={
                    consoleItem
                      .consoleId
                  }
                >
                  {
                    consoleItem
                      .manufacturerName
                  }{' '}
                  {
                    consoleItem
                      .consoleModelName
                  }{' '}
                  —{' '}
                  {
                    consoleItem
                      .serialNumber
                  }
                </option>
              ),
            )}
          </FormField>
        </div>


        <FormField
          label="Title"
          name="title"

          value={
            newRepairCase
              .title
          }

          onChange={
            handleRepairCaseChange
          }

          required
        />


        <FormField
          label="Description"
          name="description"

          value={
            newRepairCase
              .description
          }

          onChange={
            handleRepairCaseChange
          }

          textarea
          rows={3}
          maxLength={2000}
          required
        />
      </div>
    )}


    <div className="mt-8 flex justify-end">
      <button
        type="button"

        onClick={
          onContinue
        }

        disabled={
          loadingComponents
        }

        className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {loadingComponents ? (
          <>
            <LoaderCircle className="h-4 w-4 animate-spin" />

            Loading...
          </>
        ) : (
          <>
            Continue

            <ArrowRight className="h-4 w-4" />
          </>
        )}
      </button>
    </div>
  </div>
)


// STEP 2
const ComponentStep = ({
  selectedConsole,
  componentMode,
  setComponentMode,
  availableComponents,
  selectedComponentId,
  setSelectedComponentId,
  newComponent,
  handleComponentChange,
  onBack,
  onContinue,
}) => (
  <div className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
    <h2 className="text-xl font-semibold text-slate-900">
      Select Component
    </h2>


    <p className="mt-1 text-sm text-slate-500">
      Components registered for{' '}

      <strong>
        {selectedConsole
          ?.manufacturerName}{' '}

        {selectedConsole
          ?.consoleModelName}
      </strong>
      .
    </p>


    <div className="mt-6">
      <SelectionMode
        value={
          componentMode
        }

        onChange={
          setComponentMode
        }

        existingLabel="Select existing component"

        createLabel="Register new component"
      />
    </div>


    {componentMode ===
    'existing' ? (
      <div className="mt-6">
        <FormField
          label="Component"
          name="componentId"

          value={
            selectedComponentId
          }

          onChange={(
            event,
          ) =>
            setSelectedComponentId(
              event.target
                .value,
            )
          }
        >
          <option value="">
            Select a component
          </option>

          {availableComponents.map(
            (component) => (
              <option
                key={
                  component
                    .componentId
                }

                value={
                  component
                    .componentId
                }
              >
                {component.name}
              </option>
            ),
          )}
        </FormField>


        {availableComponents.length ===
          0 && (
          <p className="mt-2 text-sm text-amber-600">
            This console model has no registered components. Register one to continue.
          </p>
        )}
      </div>
    ) : (
      <div className="mt-6 grid gap-6 sm:grid-cols-2">
        <FormField
          label="Component name"
          name="name"

          value={
            newComponent.name
          }

          onChange={
            handleComponentChange
          }

          required
        />


        <FormField
          label="Description"
          name="description"

          value={
            newComponent
              .description
          }

          onChange={
            handleComponentChange
          }
        />
      </div>
    )}


    <StepButtons
      onBack={
        onBack
      }

      onContinue={
        onContinue
      }
    />
  </div>
)


// STEP 3
const MeasurementsStep = ({
  measurements,
  handleMeasurementChange,
  submitting,
  onBack,
  onSubmit,
}) => {
  const fields = [
    {
      name: 'measuredVoltage',
      label: 'Voltage',
      unit: 'V',
      icon: Zap,
    },
    {
      name: 'measuredCurrent',
      label: 'Current',
      unit: 'A',
      icon: Activity,
    },
    {
      name: 'measuredResistance',
      label: 'Resistance',
      unit: 'Ω',
      icon: Gauge,
    },
    {
      name: 'temperature',
      label: 'Temperature',
      unit: '°C',
      icon: Thermometer,
    },
  ]


  return (
    <form
      onSubmit={
        onSubmit
      }

      className="mt-6 rounded-xl border border-slate-200 bg-white p-6 shadow-sm"
    >
      <h2 className="text-xl font-semibold text-slate-900">
        Test Measurements
      </h2>


      <p className="mt-1 text-sm text-slate-500">
        All measurements are optional.
      </p>

      <div className="mt-6 grid gap-6 sm:grid-cols-2">
        {fields.map(
          (field) => {
            const Icon =
              field.icon

            return (
              <div
                key={
                  field.name
                }
              >
                <label
                  htmlFor={
                    field.name
                  }

                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  {
                    field.label
                  }{' '}

                  <span className="font-normal text-slate-400">
                    (optional)
                  </span>
                </label>


                <div className="relative">
                  <Icon className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />


                  <input
                    id={
                      field.name
                    }

                    name={
                      field.name
                    }

                    type="number"

                    min="0"

                    step="0.01"

                    value={
                      measurements[
                        field.name
                      ]
                    }

                    onChange={
                      handleMeasurementChange
                    }

                    className="w-full rounded-lg border border-slate-300 py-3 pl-11 pr-14 outline-none focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
                  />


                  <span className="absolute right-4 top-1/2 -translate-y-1/2 text-sm text-slate-400">
                    {
                      field.unit
                    }
                  </span>
                </div>
              </div>
            )
          },
        )}
      </div>


      <div className="mt-6 grid gap-6 sm:grid-cols-2">
        <FormField
          label="Result"
          name="result"

          value={
            measurements.result
          }

          onChange={
            handleMeasurementChange
          }
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


        <FormField
          label="Notes"
          name="notes"

          value={
            measurements.notes
          }

          onChange={
            handleMeasurementChange
          }

          textarea
          rows={3}
          maxLength={2000}
        />
      </div>


      <div className="mt-8 flex justify-between border-t border-slate-200 pt-6">
        <button
          type="button"

          onClick={
            onBack
          }

          disabled={
            submitting
          }

          className="flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50"
        >
          <ArrowLeft className="h-4 w-4" />

          Back
        </button>


        <button
          type="submit"

          disabled={
            submitting
          }

          className="flex items-center gap-2 rounded-lg bg-emerald-500 px-5 py-2.5 font-semibold text-white hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {submitting ? (
            <LoaderCircle className="h-4 w-4 animate-spin" />
          ) : (
            <Save className="h-4 w-4" />
          )}


          {submitting
            ? 'Creating...'
            : 'Create test'}
        </button>
      </div>
    </form>
  )
}


// nav buttons
const StepButtons = ({
  onBack,
  onContinue,
}) => (
  <div className="mt-8 flex justify-between">
    <button
      type="button"

      onClick={
        onBack
      }

      className="flex items-center gap-2 rounded-lg border border-slate-300 px-5 py-2.5 font-medium text-slate-700 hover:bg-slate-100"
    >
      <ArrowLeft className="h-4 w-4" />

      Back
    </button>


    <button
      type="button"

      onClick={
        onContinue
      }

      className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white hover:bg-primary-600"
    >
      Continue

      <ArrowRight className="h-4 w-4" />
    </button>
  </div>
)

const formatStatus = (
  status,
) =>
  status
    ?.replaceAll(
      '_',
      ' ',
    )
    .toLowerCase()
    .replace(
      /\b\w/g,
      (character) =>
        character.toUpperCase(),
    )


// extract validation or api errors into a friendly message
const getErrorMessage = (
  error,
  fallbackMessage,
) => {
  const validationErrors =
    error.response
      ?.data
      ?.errors

  if (
    Array.isArray(
      validationErrors,
    ) &&
    validationErrors.length >
      0
  ) {
    return validationErrors.join(
      ' ',
    )
  }
  return (
    error.response
      ?.data
      ?.message ??
    error.message ??
    fallbackMessage
  )
}


export default ComponentTestCreate