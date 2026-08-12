import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// Lazy loading pour améliorer les performances
const Login = lazy(() => import("./pages/Login"));
const ProtectedRoute = lazy(() => import("./components/ProtectedRoute"));
const AdminLayout = lazy(() => import("./layouts/AdminLayout"));
const AdminHome = lazy(() => import("./pages/AdminHome"));
const AdminUsers = lazy(() => import("./pages/AdminUsers"));
const AdminDossiers = lazy(() => import("./pages/AdminDossiers"));
const AdminImport = lazy(() => import("./pages/AdminImport"));
const AdminGestionMensuelle = lazy(() => import("./pages/AdminGestionMensuelle"));
const AgentDashboard = lazy(() => import("./pages/AgentDashboard"));
const DossierDetail = lazy(() => import("./pages/DossierDetail"));

// Composant de chargement
const LoadingFallback = () => (
  <div style={{ 
    display: 'flex', 
    justifyContent: 'center', 
    alignItems: 'center', 
    height: '100vh',
    fontSize: '1.2rem',
    color: '#666'
  }}>
    <div style={{
      border: '4px solid #f3f3f3',
      borderTop: '4px solid #3498db',
      borderRadius: '50%',
      width: '40px',
      height: '40px',
      animation: 'spin 1s linear infinite',
      marginRight: '15px'
    }}></div>
    Chargement...
  </div>
);

function App() {
    return (
        <BrowserRouter>
            <Suspense fallback={<LoadingFallback />}>
            <Routes>
                <Route path="/login" element={<Login />} />

                <Route path="/admin" element={<ProtectedRoute allowedRoles={["ADMIN"]}><AdminLayout /></ProtectedRoute>}>
                    <Route path="dashboard" element={<AdminHome />} />
                    <Route path="utilisateurs" element={<AdminUsers />} />
                    <Route path="dossiers" element={<AdminDossiers />} />
                    <Route path="import" element={<AdminImport />} />
                    <Route path="gestion-mensuelle" element={<AdminGestionMensuelle />} />
                </Route>

                <Route path="/agent/dashboard" element={<ProtectedRoute allowedRoles={["AGENT"]}><AgentDashboard /></ProtectedRoute>} />
                <Route path="/agent/dossiers/:id" element={<ProtectedRoute allowedRoles={["AGENT", "ADMIN"]}><DossierDetail /></ProtectedRoute>} />

                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
            </Suspense>
        </BrowserRouter>
    );
}

export default App;