import { NavLink, Outlet } from "react-router-dom";
import { logout } from "../services/authService";
import "./AdminLayout.css";

export default function AdminLayout() {
    const nom = localStorage.getItem("nom") || "Admin";

    const handleLogout = () => {
        logout();
        window.location.href = "/login";
    };

    return (
        <div className="admin-layout">
            <header className="admin-header">
                <span>Centre_Aouad</span>
                <div className="admin-header-right">
                    <span className="admin-username">{nom}</span>
                    <div className="logo-block">
                        <span className="logo-circle">C</span>
                        <div className="logo-text"><strong>eqdom</strong><small>L'essentiel, c'est vous.</small></div>
                    </div>
                </div>
            </header>

            <div className="admin-body">
                <aside className="admin-sidebar">
                    <NavLink to="/admin/dashboard" end className={({isActive}) => isActive ? "active" : ""}>
                        Tableau de bord
                    </NavLink>
                    <NavLink to="/admin/utilisateurs" className={({isActive}) => isActive ? "active" : ""}>
                        Utilisateurs
                    </NavLink>
                    <NavLink to="/admin/dossiers" className={({isActive}) => isActive ? "active" : ""}>
                        Dossiers
                    </NavLink>
                    <NavLink to="/admin/import" className={({isActive}) => isActive ? "active" : ""}>
                        Import Excel
                    </NavLink>
                    <button className="logout-btn" onClick={handleLogout}>Déconnexion</button>
                </aside>

                <main className="admin-content">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}