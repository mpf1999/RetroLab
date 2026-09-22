const FormField = ({
  label,
  name,
  value,
  onChange,
  type = 'text',
  children,
  textarea = false,
  rows = 3,
  maxLength,
  minLength,
  min,
  max,
  step,
  pattern,
  title,
  placeholder,
  autoComplete,
  disabled = false,
  required = false,
  helpText,
}) => {
  const inputStyle =
    'w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-slate-800 outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500'

  return (
    <div>
      <label
        htmlFor={name}
        className="mb-2 block text-sm font-medium text-slate-700"
      >
        {label}

        {required && (
          <span className="ml-1 text-red-500">
            *
          </span>
        )}
      </label>

      {children ? (
        <select
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          disabled={disabled}
          required={required}
          className={inputStyle}
        >
          {children}
        </select>
      ) : textarea ? (
        <textarea
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          rows={rows}
          maxLength={maxLength}
          placeholder={placeholder}
          disabled={disabled}
          required={required}
          className={inputStyle}
        />
      ) : (
        <input
          id={name}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          maxLength={maxLength}
          minLength={minLength}
          min={min}
          max={max}
          step={step}
          pattern={pattern}
          title={title}
          placeholder={placeholder}
          autoComplete={autoComplete}
          disabled={disabled}
          required={required}
          className={inputStyle}
        />
      )}

      {helpText && (
        <p className="mt-2 text-sm text-slate-500">
          {helpText}
        </p>
      )}
    </div>
  )
}

export default FormField