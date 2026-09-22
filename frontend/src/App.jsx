import {
  Route,
  Routes,
} from 'react-router-dom'

import ProtectedRoute from './components/ProtectedRoute'
import AppLayout from './components/AppLayout'

import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import Team from './pages/Team'
import Settings from './pages/Settings'

import Consoles from './pages/Consoles'
import AddConsole from './pages/AddConsole'
import ConsoleDetails from './pages/ConsoleDetails'
import EditConsole from './pages/EditConsole'

import ConsoleModels from './pages/ConsoleModels'
import ConsoleModelForm from './pages/ConsoleModelForm'
import ConsoleModelDetails from './pages/ConsoleModelDetails'

import RepairCases from './pages/RepairCases'
import RepairCaseDetails from './pages/RepairCaseDetails'

import ComponentTestCreate from './pages/ComponentTestCreate'
import AddUser from './pages/AddUser'
import UserDetails from './pages/UserDetails'
import ArchivedConsoles from './pages/ArchivedConsoles'

import RepairCaseCreate
  from './pages/RepairCaseCreate'



const NotFound = () => {
  return (
    <section>
      <h1 className="text-3xl font-bold text-slate-900">
        Page not found
      </h1>

      <p className="mt-2 text-slate-600">
        The requested page does not exist.
      </p>
    </section>
  )
}

function App() {
  return (
    <Routes>
      <Route
        path="/login"
        element={<Login />}
      />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route
            path="/"
            element={<Dashboard />}
          />

          <Route
            path="/profile"
            element={<Profile />}
          />

          <Route
            path="/team"
            element={<Team />}
          />

          <Route
            path="/settings"
            element={<Settings />}
          />

          <Route
            path="/consoles"
            element={<Consoles />}
          />

          <Route
            path="/consoles/new"
            element={<AddConsole />}
          />

          <Route
            path="/consoles/:id/edit"
            element={<EditConsole />}
          />

          <Route
            path="/consoles/:id"
            element={<ConsoleDetails />}
          />

          <Route
            path="/console-models"
            element={<ConsoleModels />}
          />

          <Route
            path="/console-models/new"
            element={<ConsoleModelForm />}
          />

          <Route
            path="/console-models/:consoleModelId"
            element={<ConsoleModelDetails />}
          />

          <Route
            path="/repair-cases"
            element={<RepairCases />}
          />

          <Route
            path="/repair-cases/:repairCaseId"
            element={<RepairCaseDetails />}
          />

          <Route
            path="/component-tests/new"
            element={<ComponentTestCreate />}
          />

          <Route
            path="/team/new"
            element={<AddUser />}
          />
          <Route
            path="/team/:userId"
            element={<UserDetails />}
          />
          <Route
            path="*"
            element={<NotFound />}
          />
          <Route
            path="/consoles/archived"
            element={<ArchivedConsoles />}
          />
          <Route
            path="/repair-cases/new"
            element={<RepairCaseCreate />}
          />
        </Route>
      </Route>
    </Routes>
  )
}

export default App