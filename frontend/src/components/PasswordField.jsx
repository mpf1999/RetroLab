import { useState } from 'react'
import {
  Eye,
  EyeOff,
  LockKeyhole,
} from 'lucide-react'

const PasswordField = ({
  label = 'Password',
  name = 'password',
  value,
  onChange,
  disabled = false,
  required = false,
  minLength,
  maxLength,
  pattern,
  title,
  placeholder,
  autoComplete,
  helpText,
}) => {
  const [visible, setVisible] =
    useState(false)

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

      <div className="relative">
        <LockKeyhole className="pointer-events-none absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />

        <input
          id={name}
          name={name}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          disabled={disabled}
          required={required}
          minLength={minLength}
          maxLength={maxLength}
          pattern={pattern}
          title={title}
          placeholder={placeholder}
          autoComplete={autoComplete}
          className="w-full rounded-lg border border-slate-300 bg-white py-3 pl-11 pr-12 text-slate-800 outline-none transition focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:cursor-not-allowed disabled:bg-slate-100 disabled:text-slate-500"
        />

        <button
          type="button"
          onClick={() =>
            setVisible((currentValue) =>
              !currentValue
            )
          }
          disabled={disabled}
          aria-label={
            visible
              ? 'Hide password'
              : 'Show password'
          }
          className="absolute right-3 top-1/2 -translate-y-1/2 rounded p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50"
        >
          {visible ? (
            <EyeOff className="h-5 w-5" />
          ) : (
            <Eye className="h-5 w-5" />
          )}
        </button>
      </div>

      {helpText && (
        <p className="mt-2 text-sm text-slate-500">
          {helpText}
        </p>
      )}
    </div>
  )
}

export default PasswordField
