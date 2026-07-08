import { Navigate } from "react-router-dom";
import { getToken, getRoles } from "../services/authService";

interface Props {
    children: React.ReactNode;
    allowedRoles?: string[];
}

export default function ProtectedRoute({ children, allowedRoles }: Props) {
    const token = getToken();
    const roles = getRoles();

    if (!token) return <Navigate to="/login" replace />;

    if (allowedRoles && !allowedRoles.some((r) => roles.includes(r))) {
        return <Navigate to="/login" replace />;
    }

    return <>{children}</>;
}