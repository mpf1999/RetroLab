import { Search } from 'lucide-react'

const SearchField = ({
  id,
  value,
  onChange,
  placeholder = 'Search...',
}) => {
  return (
    <div className="w-full">
      <label
        htmlFor={id}
        className="mb-1 block text-xs font-medium uppercase tracking-wide text-slate-500"
      >
        Search
      </label>

      <div className="relative w-full">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />

        <input
          id={id}
          type="search"
          value={value}
          onChange={(event) =>
            onChange(event.target.value)
          }
          placeholder={placeholder}
          className="h-12 w-full rounded-lg border border-slate-300 bg-white pl-12 pr-4 text-base text-slate-700 outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100"
        />
      </div>
    </div>
  )
}

export default SearchField
