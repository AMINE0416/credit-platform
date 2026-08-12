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
    const [showPassword, setShowPassword] = useState(false);
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
                    <div className="brand-text">
                        <span className="brand-name">eqdom</span>
                        <span className="brand-tagline">L'essentiel, c'est vous.</span>
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
                    <div className="password-input-container">
                        <input
                            type={showPassword ? "text" : "password"}
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            required
                        />
                        <button
                            type="button"
                            className="password-toggle-btn"
                            onClick={() => setShowPassword(!showPassword)}
                            aria-label={showPassword ? "Masquer le mot de passe" : "Afficher le mot de passe"}
                        >
                            {showPassword ? (
                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                                </svg>
                            ) : (
                                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2} stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                </svg>
                            )}
                        </button>
                    </div>

                    {error && <div className="login-error">{error}</div>}

                    <button type="submit" disabled={loading}>
                        {loading ? "Connexion..." : "Se connecter"}
                    </button>
                </form>
            </div>
        </div>
    );
}
