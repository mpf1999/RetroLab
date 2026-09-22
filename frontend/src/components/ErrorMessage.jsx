import { AlertCircle } from 'lucide-react'

const ErrorMessage = ({
  message,
  className = 'mt-6',
}) => {
  if (!message) {
    return null
  }

  return (
    <div
      role="alert"
      className={`${className} flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700`}
    >
      <AlertCircle className="mt-0.5 h-5 w-5 shrink-0" />
      <p>{message}</p>
    </div>
  )
}

export default ErrorMessage
