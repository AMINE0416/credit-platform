import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import AdminHome from "./pages/AdminHome";
import AdminUsers from "./pages/AdminUsers";
import AdminDossiers from "./pages/AdminDossiers";
import AdminImport from "./pages/AdminImport";
import AgentDashboard from "./pages/AgentDashboard";
import DossierDetail from "./pages/DossierDetail";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login />} />

                <Route path="/admin" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminLayout /></ProtectedRoute>}>
                    <Route path="dashboard" element={<AdminHome />} />
                    <Route path="utilisateurs" element={<AdminUsers />} />
                    <Route path="dossiers" element={<AdminDossiers />} />
                    <Route path="import" element={<AdminImport />} />
                </Route>

                <Route path="/agent/dashboard" element={<ProtectedRoute allowedRoles={["AGENT"]}><AgentDashboard /></ProtectedRoute>} />
                <Route path="/agent/dossiers/:id" element={<ProtectedRoute allowedRoles={["AGENT", "ADMIN"]}><DossierDetail /></ProtectedRoute>} />

                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;