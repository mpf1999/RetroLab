import { useState } from 'react'
import { Save, Coins } from 'lucide-react'
import {
  currencyCodes,
  getCurrencyLabel,
  getPreferredCurrency,
  savePreferredCurrency,
} from '../utils/currencies'
import FormField from '../components/FormField'

const Settings = () => {
  const [preferredCurrency, setPreferredCurrency] =
    useState(getPreferredCurrency)

  const [saved, setSaved] = useState(false)

  const handleSubmit = (event) => {
  event.preventDefault()

  savePreferredCurrency(
    preferredCurrency,
  )

  setSaved(true)

  window.setTimeout(() => {
    setSaved(false)
  }, 2000)
}

  return (
    <section className="mx-auto max-w-3xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900">
          Settings
        </h1>

        <p className="mt-2 text-slate-500">
          Configure your RetroLab preferences.
        </p>
      </div>

      <form
        onSubmit={handleSubmit}
        className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm"
      >
        <div className="border-b border-slate-200 p-6">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary-100 text-primary-600">
              <Coins className="h-5 w-5" />
            </div>

            <div>
              <h2 className="text-lg font-semibold text-slate-900">
                Regional preferences
              </h2>

              <p className="text-sm text-slate-500">
                Configure the default currency used by RetroLab.
              </p>
            </div>
          </div>
        </div>

        <div className="p-6">
          <FormField
            label="Default currency"
            name="preferredCurrency"
            value={preferredCurrency}
            onChange={(event) =>
              setPreferredCurrency(event.target.value)
            }
          >
            {currencyCodes.map((currencyCode) => (
              <option
                key={currencyCode}
                value={currencyCode}
              >
                {getCurrencyLabel(currencyCode)}
              </option>
            ))}
          </FormField>

          <p className="mt-2 text-sm text-slate-500">
            This currency will be selected automatically when adding a console. You can still choose a different currency for an individual console.
          </p>
        </div>

        <div className="flex items-center justify-end gap-4 border-t border-slate-200 bg-slate-50 px-6 py-4">
          {saved && (
            <p className="text-sm font-medium text-emerald-600">
              Settings saved successfully.
            </p>
          )}

          <button
            type="submit"
            className="flex items-center gap-2 rounded-lg bg-primary-500 px-5 py-2.5 font-semibold text-white transition-colors hover:bg-primary-600"
          >
            <Save className="h-5 w-5" />
            Save settings
          </button>
        </div>
      </form>
    </section>
  )
}

export default Settings