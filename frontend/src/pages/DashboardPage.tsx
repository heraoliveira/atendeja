import { useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { EmptyState, Feedback, LoadingState } from "../components/Feedback";
import { PageHeader } from "../components/PageHeader";
import { getDailyDashboard } from "../services/dashboardService";
import { listProfessionals } from "../services/professionalService";
import type { DailyDashboardResponse, ProfessionalResponse } from "../types/api";
import { appointmentStatusLabels, formatMinutes, toDateInputValue } from "../utils/formatters";
import { getApiErrorMessage } from "../utils/errors";

export function DashboardPage() {
  const [date, setDate] = useState(toDateInputValue());
  const [professionalId, setProfessionalId] = useState("");
  const [professionals, setProfessionals] = useState<ProfessionalResponse[]>([]);
  const [dashboard, setDashboard] = useState<DailyDashboardResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listProfessionals({ active: true, size: 100 })
      .then((page) => setProfessionals(page.content))
      .catch(() => setProfessionals([]));
  }, []);

  useEffect(() => {
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date, professionalId]);

  async function loadDashboard() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await getDailyDashboard({
        date,
        professionalId: professionalId ? Number(professionalId) : ""
      });
      setDashboard(response);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
      setDashboard(null);
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <section>
      <PageHeader
        title="Dashboard diário"
        description="Indicadores do dia operacional com base na agenda e na disponibilidade real dos profissionais."
        actions={(
          <button type="button" className="button-secondary" onClick={loadDashboard}>
            <RefreshCw size={16} /> Atualizar
          </button>
        )}
      />

      <div className="filter-bar">
        <label>
          Data
          <input type="date" value={date} onChange={(event) => setDate(event.target.value)} />
        </label>
        <label>
          Profissional
          <select value={professionalId} onChange={(event) => setProfessionalId(event.target.value)}>
            <option value="">Todos os profissionais</option>
            {professionals.map((professional) => (
              <option key={professional.id} value={professional.id}>{professional.name}</option>
            ))}
          </select>
        </label>
      </div>

      {error ? <Feedback type="error">{error}</Feedback> : null}
      {isLoading ? <LoadingState /> : null}
      {!isLoading && !dashboard ? <EmptyState title="Nenhum indicador disponível." /> : null}

      {dashboard ? (
        <>
          <div className="metric-grid">
            <article className="metric-card">
              <span>Agendamentos</span>
              <strong>{dashboard.totalAppointments}</strong>
            </article>
            <article className="metric-card">
              <span>Cancelamentos</span>
              <strong>{dashboard.cancellations}</strong>
            </article>
            <article className="metric-card">
              <span>Faltas</span>
              <strong>{dashboard.noShows}</strong>
            </article>
            <article className="metric-card">
              <span>Ocupação</span>
              <strong>{Number(dashboard.occupancyPercentage).toFixed(2)}%</strong>
            </article>
            <article className="metric-card">
              <span>Minutos disponíveis</span>
              <strong>{formatMinutes(dashboard.availableMinutes)}</strong>
            </article>
            <article className="metric-card">
              <span>Minutos ocupados</span>
              <strong>{formatMinutes(dashboard.occupiedMinutes)}</strong>
            </article>
          </div>

          <div className="content-panel">
            <h2>Agendamentos por status</h2>
            <div className="status-summary">
              {Object.entries(dashboard.appointmentsByStatus).map(([status, total]) => (
                <div key={status}>
                  <span>{appointmentStatusLabels[status as keyof typeof appointmentStatusLabels]}</span>
                  <strong>{total}</strong>
                </div>
              ))}
            </div>
          </div>
        </>
      ) : null}
    </section>
  );
}
