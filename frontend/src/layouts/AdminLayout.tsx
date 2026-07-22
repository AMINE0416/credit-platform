import { NavLink, Outlet } from "react-router-dom";
import { logout } from "../services/authService";
import eqdomLogo from "../assets/eqdom.jpg";
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
                <div className="header-left">
                    <img src={eqdomLogo} alt="eqdom" className="eqdom-logo" />
                    <div className="brand-text">
                        <span className="brand-name">eqdom</span>
                        <span className="brand-tagline">L'essentiel, c'est vous.</span>
                    </div>
                </div>
                <div className="admin-header-right">
                    <span className="admin-username">{nom}</span>
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
