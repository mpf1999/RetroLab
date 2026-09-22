import {
  useEffect,
  useRef,
  useState,
} from 'react'

import {
  Link,
  useNavigate,
} from 'react-router-dom'

import {
  ChevronDown,
  LoaderCircle,
  LogOut,
  Menu,
  Settings,
  User,
  X,
} from 'lucide-react'

import GlobalSearch from './GlobalSearch'
import { logout } from '../api/authApi'
import { getUsers } from '../api/userApi'

import {
  findAuthenticatedUser,
} from '../auth/currentUser'


const Navbar = ({
  sidebarOpen,
  onToggleSidebar,
}) => {

  //useNavigate allows programmatic navigation, we need to execute some logic before navigating
  const navigate = useNavigate()


  // ref to profile container, see if click is out or in
  const profileContainerRef =
    useRef(null)
  const [profileOpen, setProfileOpen] =
    useState(false)


  // store authenticated user info
  const [currentUser, setCurrentUser] =
    useState(null)


  // check if the authenticated info is still loading
  const [loadingUser, setLoadingUser] =
    useState(true)
  // load authenticated user when the navbar is mounted
  useEffect(() => {

    // prevenbt state updates if component is unmounted while request is running
    let componentMounted = true
    const loadCurrentUser = async () => {
      try {
        setLoadingUser(true)
        //retrieve users from backend
        const users = await getUsers()
        // if already mounted, stop
        if (!componentMounted) {
          return
        }


        // determine which user corresponds to the currently authenticated user
        const authenticatedUser =
          findAuthenticatedUser(
            Array.isArray(users)
              ? users
              : [],
          )

        // store authenticated user
        setCurrentUser(
          authenticatedUser ?? null,
        )

      } catch (requestError) {

        // log error, debugging
        console.error(
          'Could not load the authenticated user:',
          requestError,
        )

        // only update state if still mounted
        if (componentMounted) {
          setCurrentUser(null)
        }
      } finally {
        if (componentMounted) {
          setLoadingUser(false)
        }
      }
    }


    // execute async function
    loadCurrentUser()


    // cleanup when finished
    return () => {
      componentMounted = false
    }

  }, [])


  // Listen for changes to the authenticated user information, navbar updates without refreshing
  useEffect(() => {

    const handleUserUpdated = (event) => {
      const updatedUser =
        event.detail
      // ignore event if there is no user info
      if (!updatedUser) {
        return
      }
      // merge the previous user information with the updated fields
      setCurrentUser(
        (previousUser) => ({
          ...previousUser,
          ...updatedUser,
        }),
      )
    }
    // register custom event listener
    window.addEventListener(
      'retrolab:user-updated',
      handleUserUpdated,
    )
    // remove listener when unmounted
    return () => {
      window.removeEventListener(
        'retrolab:user-updated',
        handleUserUpdated,
      )
    }

  }, [])


  // close when clicking outside
  useEffect(() => {

    const handleOutsideClick = (event) => {
      //profileContainerRef.current refers to the real DOM element.

      if (
        profileContainerRef.current &&
        !profileContainerRef.current.contains(event.target,
        )
      ) {
        setProfileOpen(false)
      }
    }


    // listen mouse click in document
    document.addEventListener(
      'mousedown',
      handleOutsideClick,
    )


    // remove event listener when unmounted
    return () => {
      document.removeEventListener(
        'mousedown',
        handleOutsideClick,
      )
    }

  }, [])
  // close also with esc
  useEffect(() => {

    const handleKeyDown = (event) => {

      if (event.key === 'Escape') {
        setProfileOpen(false)
      }
    }
    // listen for keyboard events
    document.addEventListener(
      'keydown',
      handleKeyDown,
    )
    // remove the listener when unmounted
    return () => {
      document.removeEventListener(
        'keydown',
        handleKeyDown,
      )
    }
  }, [])

  const handleLogout = () => {
    setProfileOpen(false)
    // remove auth info
    logout()
    // redirect to logic page
    navigate('/login', {
      replace: true,
    })
  }

  // helper, profile or settings
  const closeProfileMenu = () => {
    setProfileOpen(false)
  }


  // determine name to show name > email > "User"
  const displayName =
    currentUser?.name?.trim() ||
    currentUser?.email ||
    'User'

  const displayRole = formatRole(
    currentUser?.role,
  )

  // generate initials inside profile avatar
  const userInitials = getInitials(
    currentUser?.name,
    currentUser?.email,
  )


  return (
    <header
      // navbar position depends on sidebar
      className={`fixed right-0 top-0 z-10 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 transition-all duration-300 ${
        sidebarOpen
          ? 'left-64'
          : 'left-0'
      }`}
    >
      <div className="flex flex-1 items-center gap-4">


        {/*Sidebar toggle button*/}
        <button
          type="button"
          onClick={onToggleSidebar}

          aria-label={
            sidebarOpen
              ? 'Hide sidebar'
              : 'Show sidebar'
          }
          title={
            sidebarOpen
              ? 'Hide sidebar'
              : 'Show sidebar'
          }

          className="rounded-lg p-2 text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900"
        >

          {/*
           * display X when the Sidebar is open
           * and Menu when it is closed.
           */}
          {sidebarOpen ? (
            <X className="h-6 w-6" />
          ) : (
            <Menu className="h-6 w-6" />
          )}

        </button>
        <GlobalSearch />

      </div>

      <div
        ref={profileContainerRef}
        className="relative ml-6"
      >

        {/*
         * Button used to open and close the profile dropdown
         */}
        <button
          type="button"

          onClick={() =>
            setProfileOpen(
              (currentValue) =>
                !currentValue,
            )
          }

          aria-expanded={profileOpen}
          aria-haspopup="menu"

          className="flex items-center gap-3 rounded-lg px-3 py-2 transition-colors hover:bg-slate-100"
        >


          {/* User avatar */}
          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-primary-500 text-sm font-semibold text-white">

            {/*
             * While loading show spinner, if initials exist show initials
             */}
            {loadingUser ? (
              <LoaderCircle className="h-5 w-5 animate-spin" />
            ) : userInitials ? (
              userInitials
            ) : (
              <User className="h-5 w-5" />
            )}

          </div>
          <div className="hidden text-left sm:block">

            <p className="max-w-40 truncate text-sm font-semibold text-slate-800">
              {loadingUser
                ? 'Loading...'
                : displayName}
            </p>

            <p className="text-xs text-slate-500">
              {loadingUser
                ? 'Please wait'
                : displayRole}
            </p>

          </div>


          {/*
           * arrow rotation
           */}
          <ChevronDown
            className={`h-4 w-4 shrink-0 text-slate-500 transition-transform ${
              profileOpen
                ? 'rotate-180'
                : ''
            }`}
          />

        </button>


        {/*
         * Profile dropdown
         */}
        {profileOpen && (
          <div
            role="menu"
            className="absolute right-0 top-full mt-2 w-60 overflow-hidden rounded-lg border border-slate-200 bg-white py-2 shadow-lg"
          >

            <div className="border-b border-slate-200 px-4 pb-3 pt-2">

              <p className="truncate text-sm font-semibold text-slate-900">
                {displayName}
              </p>

              <p className="mt-1 truncate text-xs text-slate-500">
                {currentUser?.email}
              </p>

            </div>

            <Link
              to="/profile"
              role="menuitem"
              onClick={closeProfileMenu}

              className="flex items-center gap-3 px-4 py-3 text-sm text-slate-700 transition-colors hover:bg-slate-100"
            >
              <User className="h-4 w-4" />

              <span>
                Profile
              </span>
            </Link>

            <Link
              to="/settings"
              role="menuitem"
              onClick={closeProfileMenu}

              className="flex items-center gap-3 px-4 py-3 text-sm text-slate-700 transition-colors hover:bg-slate-100"
            >
              <Settings className="h-4 w-4" />

              <span>
                Settings
              </span>
            </Link>
            <div className="my-1 border-t border-slate-200" />

            <button
              type="button"
              role="menuitem"
              onClick={handleLogout}

              className="flex w-full items-center gap-3 px-4 py-3 text-left text-sm text-danger-500 transition-colors hover:bg-red-50"
            >
              <LogOut className="h-4 w-4" />

              <span>
                Log out
              </span>
            </button>

          </div>
        )}

      </div>

    </header>
  )
}

const formatRole = (role) => {

  if (role === 'ADMIN') {
    return 'Administrator'
  }

  if (role === 'USER') {
    return 'Technician'
  }

  return 'Team member'
}


// generate initials

const getInitials = (
  name,
  email,
) => {
  if (name?.trim()) {
    const nameParts = name
      .trim()
      .split(/\s+/)
      .filter(Boolean)
    if (nameParts.length === 1) {
      return nameParts[0]
        .slice(0, 2)
        .toUpperCase()
    }
    return (
      nameParts[0][0] +
      nameParts[
        nameParts.length - 1
      ][0]
    ).toUpperCase()
  }

  if (email?.trim()) {
    return email
      .trim()
      .slice(0, 2)
      .toUpperCase()
  }
  return null
}


export default Navbar