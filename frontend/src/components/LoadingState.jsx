import { LoaderCircle } from 'lucide-react'

const LoadingState = ({
  message,
  className = 'min-h-96',
}) => (
  <div
    className={`flex items-center justify-center gap-3 text-slate-500 ${className}`}
  >
    <LoaderCircle className="h-7 w-7 animate-spin text-primary-500" />

    {message && <span>{message}</span>}
  </div>
)

export default LoadingState
