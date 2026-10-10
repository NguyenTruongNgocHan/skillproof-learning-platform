import { AppProviders } from "@/app/providers/AppProviders"
import { AppRouter } from "@/app/router"
import { createBrowserRouter, RouterProvider } from "react-router-dom"

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
