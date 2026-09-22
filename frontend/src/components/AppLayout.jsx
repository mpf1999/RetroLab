import {
  useState,
} from 'react'

import {
  Outlet,
} from 'react-router-dom'

import Sidebar from './Sidebar'
import Navbar from './Navbar'

// AppLayout defines common visual structure, not to repeat navbar and sidebar multiple times

const AppLayout = () => {
  // sidebar visibility
  const [
    sidebarOpen,
    setSidebarOpen,
  ] = useState(true)

  // sidebar state to its opposite when pressed
  const toggleSidebar = () => {
    setSidebarOpen(
      (currentValue) =>
        !currentValue,
    )
  }

  return (
    // main container shared by every page
    <div className="min-h-screen bg-slate-100">
      <Sidebar
        isOpen={sidebarOpen}
      />
      <Navbar
        sidebarOpen={sidebarOpen}
        onToggleSidebar={
          toggleSidebar
        }
      />

      {/*Main page content
*/}
      <main
        className={`min-h-screen pt-16 transition-all duration-300 ${
          sidebarOpen
            ? 'ml-64'
            : 'ml-0'
        }`}
      >
        <div className="p-8">
          {/*
           outlet is replaced by the component corresponding to route
           */}
          <Outlet />
        </div>
      </main>
    </div>
  )
}

export default AppLayout