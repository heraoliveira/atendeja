import { CalendarDays, Gauge, LogOut, Scissors, Users, UserRoundCheck } from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

export function AppLayout() {
  const { user, logout, hasRole } = useAuth();

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <strong>AtendeJá</strong>
          <span>Operação diária</span>
        </div>
        <nav className="main-nav" aria-label="Navegação principal">
          <NavLink to="/dashboard">
            <Gauge size={18} /> Dashboard
          </NavLink>
          <NavLink to="/appointments">
            <CalendarDays size={18} /> Agenda
          </NavLink>
          <NavLink to="/customers">
            <Users size={18} /> Clientes
          </NavLink>
          <NavLink to="/professionals">
            <UserRoundCheck size={18} /> Profissionais
          </NavLink>
          <NavLink to="/services">
            <Scissors size={18} /> Serviços
          </NavLink>
        </nav>
        <div className="session-card">
          <span>{user?.email}</span>
          <strong>{user?.role === "ADMIN" ? "Administrador" : "Atendente"}</strong>
          {!hasRole(["ADMIN"]) ? <small>Ações administrativas ficam ocultas para seu perfil.</small> : null}
          <button type="button" className="button-secondary button-full" onClick={logout}>
            <LogOut size={16} /> Sair
          </button>
        </div>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
