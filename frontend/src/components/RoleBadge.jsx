import {
  Shield,
  UserRound,
} from 'lucide-react'

const RoleBadge = ({ role }) => {
  const administrator = role === 'ADMIN'

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ${
        administrator
          ? 'bg-purple-100 text-purple-700'
          : 'bg-blue-100 text-blue-700'
      }`}
    >
      {administrator ? (
        <Shield className="h-3.5 w-3.5" />
      ) : (
        <UserRound className="h-3.5 w-3.5" />
      )}

      <span>
        {administrator
          ? 'Administrator'
          : 'Technician'}
      </span>
    </span>
  )
}

export default RoleBadge
