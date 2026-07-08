import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import AgentDashboard from "./pages/AgentDashboard";
import DossierDetail from "./pages/DossierDetail";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />
                <Route
                    path="/agent/dashboard"
                    element={<ProtectedRoute allowedRoles={["AGENT"]}><AgentDashboard /></ProtectedRoute>}
                />
                <Route
                    path="/agent/dossiers/:id"
                    element={<ProtectedRoute allowedRoles={["AGENT", "ADMIN"]}><DossierDetail /></ProtectedRoute>}
                />
                <Route
                    path="/admin/dashboard"
                    element={<ProtectedRoute allowedRoles={["ADMIN"]}><div>Dashboard Admin (à construire)</div></ProtectedRoute>}
                />
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;