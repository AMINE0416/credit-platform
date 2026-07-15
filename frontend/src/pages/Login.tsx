import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../services/authService.ts";
import eqdomLogo from "../assets/eqdom.jpg";
import "./login.css";

export default function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError("");
        setLoading(true);
        try {
            const data = await login(email, password);
            localStorage.setItem("token", data.token);
            localStorage.setItem("roles", JSON.stringify(data.roles));
            localStorage.setItem("nom", `${data.prenom} ${data.nom}`);

            if (data.roles.includes("ADMIN")) {
                navigate("/admin/dashboard");
            } else {
                navigate("/agent/dashboard");
            }
        } catch (err: any) {
            setError(err.message || "Email ou mot de passe incorrect");
            console.error("Erreur de connexion:", err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-page">
            <div className="login-header">
                <div className="header-left">
                    <img src={eqdomLogo} alt="eqdom" className="eqdom-logo-main" />
                    <span className="login-header-title">Centre_Aouad</span>
                </div>
                <div className="login-header-logo">
                    <div className="logo-text">
                        <strong>eqdom</strong>
                        <small>L'essentiel, c'est vous.</small>
                    </div>
                </div>
            </div>

            <div className="login-container">
                <form className="login-card" onSubmit={handleSubmit}>
                    <h2>Connexion</h2>
                    <p className="login-subtitle">Plateforme de gestion des dossiers clients/crédits</p>

                    <label>Email</label>
                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="prenom.nom@eqdom.ma"
                        required
                    />

                    <label>Mot de passe</label>
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                    />

                    {error && <div className="login-error">{error}</div>}

                    <button type="submit" disabled={loading}>
                        {loading ? "Connexion..." : "Se connecter"}
                    </button>
                </form>
            </div>
        </div>
    );
}
