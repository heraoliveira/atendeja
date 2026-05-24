import { useState } from "react";
import { useForm } from "react-hook-form";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { CalendarCheck } from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import type { LoginRequest } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, formState: { errors } } = useForm<LoginRequest>();
  const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? "/dashboard";

  if (isAuthenticated) {
    return <Navigate to={target} replace />;
  }

  async function onSubmit(values: LoginRequest) {
    setIsSubmitting(true);
    setError(null);
    try {
      await login(values);
      navigate(target, { replace: true });
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel" aria-labelledby="login-title">
        <div className="login-brand">
          <CalendarCheck size={36} />
          <div>
            <strong>AtendeJá</strong>
            <span>Agenda e atendimento</span>
          </div>
        </div>
        <h1 id="login-title">Entrar no sistema</h1>
        <p>Use uma conta cadastrada para acessar a operação diária.</p>

        <form onSubmit={handleSubmit(onSubmit)} className="form-stack">
          <label>
            E-mail
            <input
              type="email"
              autoComplete="email"
              {...register("email", { required: "Informe o e-mail." })}
            />
            {errors.email ? <small className="field-error">{errors.email.message}</small> : null}
          </label>

          <label>
            Senha
            <input
              type="password"
              autoComplete="current-password"
              {...register("password", { required: "Informe a senha." })}
            />
            {errors.password ? <small className="field-error">{errors.password.message}</small> : null}
          </label>

          {error ? <div className="feedback feedback-error">{error}</div> : null}

          <button type="submit" className="button-primary button-full" disabled={isSubmitting}>
            {isSubmitting ? "Entrando..." : "Entrar"}
          </button>
        </form>
      </section>
    </main>
  );
}
