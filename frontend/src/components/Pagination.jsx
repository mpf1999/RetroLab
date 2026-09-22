import {
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'

const Pagination = ({
  currentPage,
  totalPages,
  onPageChange,
}) => {
  if (totalPages <= 1) {
    return null
  }

  return (
    <div className="mt-8 flex flex-wrap items-center justify-center gap-2">
      <button
        type="button"
        onClick={() =>
          onPageChange(
            Math.max(currentPage - 1, 1),
          )
        }
        disabled={currentPage === 1}
        aria-label="Previous page"
        className="flex h-10 w-10 items-center justify-center rounded-lg border border-slate-300 bg-white text-slate-600 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronLeft className="h-5 w-5" />
      </button>

      {Array.from(
        { length: totalPages },
        (_, index) => index + 1,
      ).map((page) => (
        <button
          key={page}
          type="button"
          onClick={() => onPageChange(page)}
          aria-current={
            currentPage === page
              ? 'page'
              : undefined
          }
          className={`h-10 min-w-10 rounded-lg px-3 text-sm font-semibold transition-colors ${
            currentPage === page
              ? 'bg-primary-500 text-white'
              : 'border border-slate-300 bg-white text-slate-600 hover:bg-slate-100'
          }`}
        >
          {page}
        </button>
      ))}

      <button
        type="button"
        onClick={() =>
          onPageChange(
            Math.min(
              currentPage + 1,
              totalPages,
            ),
          )
        }
        disabled={currentPage === totalPages}
        aria-label="Next page"
        className="flex h-10 w-10 items-center justify-center rounded-lg border border-slate-300 bg-white text-slate-600 transition-colors hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
      >
        <ChevronRight className="h-5 w-5" />
      </button>
    </div>
  )
}

export default Pagination