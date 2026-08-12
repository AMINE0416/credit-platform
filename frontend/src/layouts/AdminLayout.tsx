import { NavLink, Outlet } from "react-router-dom";
import { logout } from "../services/authService";
import eqdomLogo from "../assets/eqdom.jpg";
import "./AdminLayout.css";

/* ─── SVG Icons ──────────────────────────────────────────── */
const IconDashboard = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
    <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
  </svg>
);
const IconUsers = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
    <circle cx="9" cy="7" r="4"/>
    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
  </svg>
);
const IconFolders = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
  </svg>
);
const IconUpload = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
    <polyline points="17 8 12 3 7 8"/>
    <line x1="12" y1="3" x2="12" y2="15"/>
  </svg>
);
const IconCalendar = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
    <line x1="16" y1="2" x2="16" y2="6"/>
    <line x1="8" y1="2" x2="8" y2="6"/>
    <line x1="3" y1="10" x2="21" y2="10"/>
  </svg>
);
const IconLogout = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
    <polyline points="16 17 21 12 16 7"/>
    <line x1="21" y1="12" x2="9" y2="12"/>
  </svg>
);
const IconUser = () => (
  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
  </svg>
);

export default function AdminLayout() {
  const nom = localStorage.getItem("nom") || "Admin";

  const handleLogout = () => {
    logout();
    window.location.href = "/login";
  };

  return (
    <div className="admin-layout">
      <header className="admin-header">
        <div className="header-left">
          <img src={eqdomLogo} alt="eqdom" className="eqdom-logo" />
          <div className="brand-text">
            <span className="brand-name">eqdom</span>
            <span className="brand-tagline">L'essentiel, c'est vous.</span>
          </div>
        </div>
        <div className="admin-header-right">
          <div className="user-chip">
            <IconUser />
            <span>{nom}</span>
          </div>
        </div>
      </header>

      <div className="admin-body">
        <aside className="admin-sidebar">
          <nav className="sidebar-nav">
            <NavLink to="/admin/dashboard" end className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
              <IconDashboard />
              <span>Tableau de bord</span>
            </NavLink>
            <NavLink to="/admin/utilisateurs" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
              <IconUsers />
              <span>Utilisateurs</span>
            </NavLink>
            <NavLink to="/admin/dossiers" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
              <IconFolders />
              <span>Dossiers</span>
            </NavLink>
            <NavLink to="/admin/import" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
              <IconUpload />
              <span>Import Excel</span>
            </NavLink>
            <NavLink to="/admin/gestion-mensuelle" className={({ isActive }) => isActive ? "nav-link active" : "nav-link"}>
              <IconCalendar />
              <span>Gestion Mensuelle</span>
            </NavLink>
          </nav>

          <div className="sidebar-footer">
            <button className="logout-btn" onClick={handleLogout}>
              <IconLogout />
              Déconnexion
            </button>
          </div>
        </aside>

        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
