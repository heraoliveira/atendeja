import { useState } from "react";
import { useForm } from "react-hook-form";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { CalendarCheck } from "lucide-react";
import { useAuth } from "../contexts/AuthContext";
import type { LoginRequest } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";

const DEMO_ADMIN_EMAIL = import.meta.env.VITE_DEMO_ADMIN_EMAIL?.trim() || "demo@atendeja.com";
const DEMO_ADMIN_PASSWORD = import.meta.env.VITE_DEMO_ADMIN_PASSWORD?.trim() || "Demo@AtendeJa";

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { register, handleSubmit, setValue, formState: { errors } } = useForm<LoginRequest>();
  const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? "/dashboard";

  if (isAuthenticated) {
    return <Navigate to={target} replace />;
  }

  function fillDemoAccount() {
    setValue("email", DEMO_ADMIN_EMAIL, { shouldDirty: true, shouldTouch: true, shouldValidate: true });
    setValue("password", DEMO_ADMIN_PASSWORD, { shouldDirty: true, shouldTouch: true, shouldValidate: true });
    setError(null);
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
      <div className="login-layout">
        <section className="demo-access-panel" aria-labelledby="demo-access-title">
          <div className="login-brand">
            <CalendarCheck size={38} />
            <div>
              <strong>AtendeJá</strong>
              <span>Agenda e atendimento</span>
            </div>
          </div>

          <div className="demo-access-copy">
            <h2 id="demo-access-title">Acesso de demonstração</h2>
            <p>Teste o sistema com uma conta pronta.</p>
          </div>

          <dl className="demo-credentials">
            <div className="demo-credential-row">
              <dt>E-mail</dt>
              <dd>{DEMO_ADMIN_EMAIL}</dd>
            </div>
            <div className="demo-credential-row">
              <dt>Senha</dt>
              <dd>{DEMO_ADMIN_PASSWORD}</dd>
            </div>
          </dl>

          <button type="button" className="button-secondary button-full" onClick={fillDemoAccount}>
            Usar conta demo
          </button>
        </section>

        <section className="login-panel" aria-labelledby="login-title">
          <div className="login-form-header">
            <h1 id="login-title">Entrar no sistema</h1>
            <p>Use uma conta cadastrada para acessar a operação diária.</p>
          </div>

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
      </div>
    </main>
  );
}
