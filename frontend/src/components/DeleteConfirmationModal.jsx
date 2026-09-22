import { AlertTriangle, LoaderCircle, X } from 'lucide-react'

const DeleteConfirmationModal = ({
  open,
  title,
  message,
  onConfirm,
  onCancel,
  deleting = false,
}) => {
  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/50 p-4" role="dialog" aria-modal="true">
      <div className="w-full max-w-md rounded-xl bg-white shadow-xl">
        <div className="flex items-start justify-between gap-4 border-b border-slate-200 p-6">
          <div className="flex items-start gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-100 text-red-600">
              <AlertTriangle className="h-5 w-5" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
              <p className="mt-2 text-sm leading-6 text-slate-600">{message}</p>
            </div>
          </div>
          <button type="button" onClick={onCancel} disabled={deleting} className="rounded-lg p-1 text-slate-400 hover:bg-slate-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="flex justify-end gap-3 p-4">
          <button type="button" onClick={onCancel} disabled={deleting} className="rounded-lg border border-slate-300 bg-white px-4 py-2.5 font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-50">
            Cancel
          </button>
          <button type="button" onClick={onConfirm} disabled={deleting} className="flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2.5 font-semibold text-white hover:bg-red-700 disabled:opacity-50">
            {deleting && <LoaderCircle className="h-4 w-4 animate-spin" />}
            {deleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default DeleteConfirmationModal
