// router used to protect routes
import {
  Navigate,
  //renders the child route when allowed
  Outlet,
  //give info abt current route
  useLocation,
} from 'react-router-dom'
import { isAuthenticated } from '../auth/authStorage'

// acts as a guard for the protected route,m redirected to login if the user is not authenticated
const ProtectedRoute = () => {
  const location = useLocation()

  if (!isAuthenticated()) {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: location.pathname,
        }}
      />
    )
  }

  return <Outlet/>
}

export default ProtectedRoute