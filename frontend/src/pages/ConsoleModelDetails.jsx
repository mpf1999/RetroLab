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
  ArrowLeft,
  Boxes,
  CalendarDays,
  Cpu,
  LoaderCircle,
  Pencil,
  Plus,
  RefreshCw,
  Save,
  Trash2,
  X,
} from 'lucide-react'
import {
  deleteConsoleModel,
  getConsoleModelById,
  updateConsoleModel,
} from '../api/consoleModelApi'
import {
  createComponent,
  deleteComponent,
  getComponentsByConsoleModelId,
  updateComponent,
} from '../api/componentApi'
import {
  getManufacturers,
} from '../api/manufacturerApi'

import DeleteConfirmationModal from '../components/DeleteConfirmationModal'
import FormField from '../components/FormField'

// empty state for resetting
const emptyComponentForm = {
  name: '',
  description: '',
}

const ConsoleModelDetails = () => {
  const { consoleModelId } = useParams()
  const navigate = useNavigate()

  // main data
  const [consoleModel, setConsoleModel] =
    useState(null)
  const [components, setComponents] =
    useState([])
  const [manufacturers, setManufacturers] =
    useState([])

  // controlled form state for when editing the console model
  const [modelForm, setModelForm] =
    useState({
      consoleModelName: '',
      manufacturerId: '',
      releaseYear: '',
    })

  const [componentForm, setComponentForm] =
    useState(emptyComponentForm)

  const [editingModel, setEditingModel] =
    useState(false)
  const [
    componentFormOpen,
    setComponentFormOpen,
  ] = useState(false)

  const [
    editingComponentId,
    setEditingComponentId,
  ] = useState(null)

  // request states to prevent duplicate operations
  const [loading, setLoading] =
    useState(true)

  const [savingModel, setSavingModel] =
    useState(false)

  const [
    savingComponent,
    setSavingComponent,
  ] = useState(false)

  const [
    deletingModel,
    setDeletingModel,
  ] = useState(false)

  const [
    deletingComponentId,
    setDeletingComponentId,
  ] = useState(null)

  const [error, setError] = useState('')

  // stores resource waiting for deletion
  const [deleteConfirmation, setDeleteConfirmation] =
    useState(null)

  // reload all info required by page
  const loadData = async () => {
    setLoading(true)
    setError('')
    try {
      const [
        modelData,
        componentsData,
        manufacturersData,
      ] = await Promise.all([
        getConsoleModelById(
          consoleModelId,
        ),

        getComponentsByConsoleModelId(
          consoleModelId,
        ),
        getManufacturers(),
      ])

      setConsoleModel(modelData)

      setComponents(
        Array.isArray(componentsData)
          ? componentsData
          : [],
      )

      setManufacturers(
        Array.isArray(manufacturersData)
          ? manufacturersData
          : [],
      )

      setModelForm({
        consoleModelName:
          modelData.consoleModelName ?? '',

        manufacturerId:
          modelData.manufacturerId?.toString() ??
          '',

        releaseYear:
          modelData.releaseYear?.toString() ??
          '',
      })
    } catch (requestError) {
      setConsoleModel(null)

      setError(
        requestError.message ||
          'Could not load the console model.',
      )
    } finally {
      setLoading(false)
    }
  }

  // load selected console model
  useEffect(() => {
    let componentMounted = true

    const loadInitialData = async () => {
      setLoading(true)
      setError('')
      try {
        const [
          modelData,
          componentsData,
          manufacturersData,
        ] = await Promise.all([
          getConsoleModelById(
            consoleModelId,
          ),

          getComponentsByConsoleModelId(
            consoleModelId,
          ),

          getManufacturers(),
        ])

        if (!componentMounted) {
          return
        }

        setConsoleModel(modelData)

        setComponents(
          Array.isArray(componentsData)
            ? componentsData
            : [],
        )

        setManufacturers(
          Array.isArray(manufacturersData)
            ? manufacturersData
            : [],
        )

        setModelForm({
          consoleModelName:
            modelData.consoleModelName ?? '',

          manufacturerId:
            modelData.manufacturerId?.toString() ??
            '',

          releaseYear:
            modelData.releaseYear?.toString() ??
            '',
        })
      } catch (requestError) {
        if (!componentMounted) {
          return
        }

        setConsoleModel(null)

        setError(
          requestError.message ||
            'Could not load the console model.',
        )
      } finally {
        if (componentMounted) {
          setLoading(false)
        }
      }
    }

    loadInitialData()

    return () => {
      componentMounted = false
    }
  }, [consoleModelId])

  // generic controlled handler for console model form
  const handleModelChange = (event) => {
    const { name, value } = event.target

    setModelForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }))
  }

  //copy the persisted model values into form before editting
  const startEditingModel = () => {
    setError('')

    setModelForm({
      consoleModelName:
        consoleModel.consoleModelName ?? '',

      manufacturerId:
        consoleModel.manufacturerId?.toString() ??
        '',

      releaseYear:
        consoleModel.releaseYear?.toString() ??
        '',
    })

    setEditingModel(true)
  }

  // leave edit mode without persisting
  const cancelEditingModel = () => {
    setError('')
    setEditingModel(false)
  }

  // validate form and persist changed
  const handleModelSubmit = async (
    event,
  ) => {
    event.preventDefault()
    setError('')

    if (
      !modelForm.consoleModelName.trim()
    ) {
      setError(
        'Console model name is required.',
      )
      return
    }

    if (!modelForm.manufacturerId) {
      setError('Select a manufacturer.')
      return
    }

    const releaseYear = Number(
      modelForm.releaseYear,
    )

    if (!Number.isInteger(releaseYear)) {
      setError('Enter a valid release year.')
      return
    }

    setSavingModel(true)
    try {
      const updatedModel =
        await updateConsoleModel(
          consoleModelId,
          modelForm,
        )

      setConsoleModel(updatedModel)

      setModelForm({
        consoleModelName:
          updatedModel.consoleModelName ?? '',

        manufacturerId:
          updatedModel.manufacturerId?.toString() ??
          '',

        releaseYear:
          updatedModel.releaseYear?.toString() ??
          '',
      })

      setEditingModel(false)
    } catch (requestError) {
      setError(
        requestError.message ||
          'Could not update the console model.',
      )
    } finally {
      setSavingModel(false)
    }
  }

  // generic handler for component form
  const handleComponentChange = (
    event,
  ) => {
    const { name, value } = event.target
    setComponentForm((currentForm) => ({
      ...currentForm,
      [name]: value,
    }))
  }

  // reset shared component form before creating a new comp
  const openNewComponentForm = () => {
    setError('')
    setEditingComponentId(null)
    setComponentForm(emptyComponentForm)
    setComponentFormOpen(true)
  }

  // populate shared component form before editing
  const openEditComponentForm = (
    component,
  ) => {
    setError('')

    setEditingComponentId(
      component.componentId,
    )

    setComponentForm({
      name: component.name ?? '',
      description:
        component.description ?? '',
    })

    setComponentFormOpen(true)
  }

  // close and reset comp form
  const closeComponentForm = () => {
    setComponentFormOpen(false)
    setEditingComponentId(null)
    setComponentForm(emptyComponentForm)
  }

  const handleComponentSubmit = async (
    event,
  ) => {
    event.preventDefault()
    setError('')

    if (!componentForm.name.trim()) {
      setError('Component name is required.')
      return
    }

    setSavingComponent(true)

    try {
      if (editingComponentId !== null) {
        const updatedComponent =
          await updateComponent(
            editingComponentId,
            {
              consoleModelId,
              name: componentForm.name,
              description:
                componentForm.description,
            },
          )

        setComponents(
          (currentComponents) =>
            currentComponents.map(
              (component) =>
                component.componentId ===
                updatedComponent.componentId
                  ? updatedComponent
                  : component,
            ),
        )
      } else {
        const createdComponent =
          await createComponent({
            consoleModelId,
            name: componentForm.name,
            description:
              componentForm.description,
          })

        setComponents(
          (currentComponents) => [
            ...currentComponents,
            createdComponent,
          ],
        )
      }

      closeComponentForm()
    } catch (requestError) {
      setError(
        requestError.message ||
          'Could not save the component.',
      )
    } finally {
      setSavingComponent(false)
    }
  }

  const handleDeleteComponent = (component) => {
    setDeleteConfirmation({
      type: 'component',
      component,
      title: `Delete component "${component.name}"?`,
      message:
        'This component and all associated component tests will be permanently deleted. This action cannot be undone.',
    })
  }

  // execute the confirmed deletion and remove from local state without reloading
  const confirmDeleteComponent = async () => {
    const component = deleteConfirmation.component
    setError('')
    setDeletingComponentId(component.componentId)

    try {
      await deleteComponent(component.componentId)

      setComponents((currentComponents) =>
        currentComponents.filter(
          (currentComponent) =>
            currentComponent.componentId !== component.componentId,
        ),
      )

      if (editingComponentId === component.componentId) {
        closeComponentForm()
      }
    } catch (requestError) {
      setError(
        requestError.message ||
          'Could not delete the component.',
      )
    } finally {
      setDeletingComponentId(null)
      setDeleteConfirmation(null)
    }
  }

  // requiring confirmation
  const handleDeleteModel = () => {
    setDeleteConfirmation({
      type: 'model',
      title: `Delete "${consoleModel.consoleModelName}"?`,
      message:
        'This console model, its registered components, associated consoles, repair cases and component tests will be permanently deleted. This action cannot be undone.',
    })
  }

  const confirmDeleteModel = async () => {
    setDeletingModel(true)
    setError('')

    try {
      await deleteConsoleModel(consoleModelId)
      navigate('/console-models', { replace: true })
    } catch (requestError) {
      setError(
        requestError.message ||
          'Could not delete the console model.',
      )
      setDeletingModel(false)
      setDeleteConfirmation(null)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-[60vh] items-center justify-center gap-3 text-slate-500">
        <LoaderCircle className="h-6 w-6 animate-spin" />
        <span>Loading console model...</span>
      </div>
    )
  }

  if (!consoleModel) {
    return (
      <section className="mx-auto max-w-5xl">
        <Link
          to="/console-models"
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-500"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back to console models</span>
        </Link>

        <div className="mt-8 rounded-xl border border-red-200 bg-white p-12 text-center">
          <Boxes className="mx-auto h-12 w-12 text-slate-300" />

          <h1 className="mt-4 text-2xl font-bold text-slate-900">
            Could not load console model
          </h1>

          <p className="mt-2 text-slate-500">
            {error ||
              'Console model not found.'}
          </p>

          <button
            type="button"
            onClick={loadData}
            className="mx-auto mt-6 flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5"
          >
            <RefreshCw className="h-4 w-4" />
            <span>Try again</span>
          </button>
        </div>
      </section>
    )
  }

  return (
    <section className="mx-auto max-w-6xl">
      <Link
        to="/console-models"
        className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-slate-800"
      >
        <ArrowLeft className="h-4 w-4" />
        <span>Back to console models</span>
      </Link>

      {error && (
        <div
          role="alert"
          className="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600"
        >
          {error}
        </div>
      )}

      <div className="mt-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-col justify-between gap-4 border-b border-slate-200 p-6 sm:flex-row sm:items-center">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
              <Boxes className="h-7 w-7" />
            </div>

            <div>
              <p className="text-sm font-medium text-primary-600">
                {consoleModel.manufacturerName}
              </p>

              <h1 className="text-2xl font-bold text-slate-900">
                {consoleModel.consoleModelName}
              </h1>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            {!editingModel && (
              <button
                type="button"
                onClick={startEditingModel}
                className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100"
              >
                <Pencil className="h-4 w-4" />
                <span>Edit model</span>
              </button>
            )}

            <button
              type="button"
              onClick={handleDeleteModel}
              disabled={deletingModel}
              className="flex items-center gap-2 rounded-lg border border-red-200 px-4 py-2.5 font-medium text-red-600 hover:bg-red-50 disabled:opacity-60"
            >
              {deletingModel ? (
                <LoaderCircle className="h-4 w-4 animate-spin" />
              ) : (
                <Trash2 className="h-4 w-4" />
              )}

              <span>Delete model</span>
            </button>
          </div>
        </div>

        {editingModel ? (
          <form
            onSubmit={handleModelSubmit}
            className="p-6"
          >
            <div className="grid gap-6 sm:grid-cols-3">
              <FormField
                label="Model name"
                name="consoleModelName"
                value={modelForm.consoleModelName}
                onChange={handleModelChange}
                required
                minLength={2}
                maxLength={80}
                disabled={savingModel}
              />

              <FormField
                label="Manufacturer"
                name="manufacturerId"
                value={modelForm.manufacturerId}
                onChange={handleModelChange}
                required
                disabled={savingModel}
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
                      </option>
                    ),
                  )}
                </FormField>

              <FormField
                label="Release year"
                name="releaseYear"
                type="number"
                value={modelForm.releaseYear}
                onChange={handleModelChange}
                required
                disabled={savingModel}
              />
            </div>

            <div className="mt-6 flex justify-end gap-3">
              <button
                type="button"
                onClick={cancelEditingModel}
                disabled={savingModel}
                className="flex items-center gap-2 rounded-lg border border-slate-300 px-4 py-2.5 text-slate-700"
              >
                <X className="h-4 w-4" />
                <span>Cancel</span>
              </button>

              <button
                type="submit"
                disabled={savingModel}
                className="flex items-center gap-2 rounded-lg bg-primary-500 px-4 py-2.5 font-semibold text-white disabled:opacity-60"
              >
                {savingModel ? (
                  <LoaderCircle className="h-4 w-4 animate-spin" />
                ) : (
                  <Save className="h-4 w-4" />
                )}

                <span>Save changes</span>
              </button>
            </div>
          </form>
        ) : (
          <div className="flex flex-wrap gap-12 p-6">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Manufacturer
              </p>

              <p className="mt-1 font-medium text-slate-800">
                {consoleModel.manufacturerName}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Release year
              </p>

              <p className="mt-1 flex items-center gap-2 font-medium text-slate-800">
                <CalendarDays className="h-4 w-4" />
                {consoleModel.releaseYear}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Registered components
              </p>

              <p className="mt-1 flex items-center gap-2 font-medium text-slate-800">
                <Cpu className="h-4 w-4" />
                {components.length}
              </p>
            </div>
          </div>
        )}
      </div>

      <div className="mt-6 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
        <div className="flex flex-col justify-between gap-4 border-b border-slate-200 p-6 sm:flex-row sm:items-center">
          <div>
            <h2 className="text-xl font-semibold text-slate-900">
              Registered Components
            </h2>

            <p className="mt-1 text-sm text-slate-500">
              Components available for tests on this model.
            </p>
          </div>

          <button
            type="button"
            onClick={openNewComponentForm}
            className="flex items-center justify-center gap-2 rounded-lg bg-emerald-500 px-4 py-2.5 font-semibold text-white hover:bg-emerald-600"
          >
            <Plus className="h-4 w-4" />
            <span>Add component</span>
          </button>
        </div>

        {componentFormOpen && (
          <form
            onSubmit={handleComponentSubmit}
            className="border-b border-slate-200 bg-slate-50 p-6"
          >
            <h3 className="font-semibold text-slate-900">
              {editingComponentId !== null
                ? 'Edit component'
                : 'Add component'}
            </h3>

            <div className="mt-4 grid gap-4 sm:grid-cols-2">
              <FormField
                label="Component name"
                name="name"
                value={componentForm.name}
                onChange={handleComponentChange}
                required
                minLength={2}
                maxLength={80}
                disabled={savingComponent}
              />

              <FormField
                label="Description"
                name="description"
                value={componentForm.description}
                onChange={handleComponentChange}
                textarea
                rows={3}
                maxLength={1000}
                disabled={savingComponent}
              />
            </div>

            <div className="mt-4 flex justify-end gap-3">
              <button
                type="button"
                onClick={closeComponentForm}
                disabled={savingComponent}
                className="rounded-lg border border-slate-300 px-4 py-2"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={savingComponent}
                className="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 font-semibold text-white disabled:opacity-60"
              >
                {savingComponent ? (
                  <LoaderCircle className="h-4 w-4 animate-spin" />
                ) : (
                  <Save className="h-4 w-4" />
                )}

                <span>
                  {editingComponentId !== null
                    ? 'Save component'
                    : 'Add component'}
                </span>
              </button>
            </div>
          </form>
        )}

        {components.length > 0 ? (
          <div className="divide-y divide-slate-100">
            {components.map((component) => (
              <div
                key={component.componentId}
                className="flex flex-col justify-between gap-4 p-6 sm:flex-row sm:items-center"
              >
                <div className="flex min-w-0 items-start gap-4">
                  <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                    <Cpu className="h-5 w-5" />
                  </div>

                  <div>
                    <h3 className="font-semibold text-slate-900">
                      {component.name}
                    </h3>

                    <p className="mt-1 text-sm text-slate-500">
                      {component.description ||
                        'No description available.'}
                    </p>
                  </div>
                </div>

                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() =>
                      openEditComponentForm(
                        component,
                      )
                    }
                    className="flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
                  >
                    <Pencil className="h-4 w-4" />
                    <span>Edit</span>
                  </button>

                  <button
                    type="button"
                    onClick={() =>
                      handleDeleteComponent(
                        component,
                      )
                    }
                    disabled={
                      deletingComponentId ===
                      component.componentId
                    }
                    className="flex items-center gap-2 rounded-lg border border-red-200 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-60"
                  >
                    {deletingComponentId ===
                    component.componentId ? (
                      <LoaderCircle className="h-4 w-4 animate-spin" />
                    ) : (
                      <Trash2 className="h-4 w-4" />
                    )}

                    <span>Delete</span>
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-12 text-center">
            <Cpu className="mx-auto h-12 w-12 text-slate-300" />

            <h3 className="mt-4 font-semibold text-slate-800">
              No components registered
            </h3>

            <p className="mt-2 text-sm text-slate-500">
              Add the first component for this console model.
            </p>
          </div>
        )}
      </div>
      <DeleteConfirmationModal
        open={deleteConfirmation !== null}
        title={deleteConfirmation?.title}
        message={deleteConfirmation?.message}
        deleting={
          deleteConfirmation?.type === 'model'
            ? deletingModel
            : deletingComponentId !== null
        }
        onCancel={() => setDeleteConfirmation(null)}
        onConfirm={
          deleteConfirmation?.type === 'model'
            ? confirmDeleteModel
            : confirmDeleteComponent
        }
      />

    </section>
  )
}

export default ConsoleModelDetails