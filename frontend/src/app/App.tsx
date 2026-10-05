import { createBrowserRouter, RouterProvider } from "react-router-dom"
import { AppProviders } from "./providers/AppProviders"
import { AppRouter } from "./router"

// Keep the existing route tree while providing the data-router navigation blocker.
const router = createBrowserRouter([
  {
    path: "*",
    element: (
      <AppProviders>
        <AppRouter />
      </AppProviders>
    ),
  },
])
export default function App() {
  return <RouterProvider router={router} />
}
