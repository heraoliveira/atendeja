import { useEffect, useMemo, useState } from "react";
import { CalendarPlus, RefreshCw, Save } from "lucide-react";
import { useForm } from "react-hook-form";
import { EmptyState, Feedback, LoadingState } from "../components/Feedback";
import { PageHeader } from "../components/PageHeader";
import { Pagination } from "../components/Pagination";
import { StatusBadge } from "../components/StatusBadge";
import { AsyncClientSelect } from "../components/AsyncClientSelect";
import {
  cancelAppointment,
  checkInAppointment,
  completeAppointment,
  confirmAppointment,
  createAppointment,
  listAppointments,
  markAppointmentNoShow,
  rescheduleAppointment
} from "../services/appointmentService";
import { listProfessionals } from "../services/professionalService";
import { listServices } from "../services/serviceCatalogService";
import type {
  AppointmentResponse,
  AppointmentStatus,
  CustomerResponse,
  PageResponse,
  ProfessionalResponse,
  ServiceResponse
} from "../types/api";
import {
  appointmentStatusLabels,
  appointmentStatusOptions,
  formatDateTime,
  localDateTimeToInstant,
  toDateInputValue,
  toLocalDateTimeInputValue
} from "../utils/formatters";
import { getApiErrorMessage } from "../utils/errors";

interface AppointmentFormValues {
  customerId: string;
  professionalId: string;
  serviceId: string;
  startAt: string;
}

type OperationType = "reschedule" | "cancel" | "noShow";

interface OperationState {
  type: OperationType;
  appointment: AppointmentResponse;
}

const emptyPage: PageResponse<AppointmentResponse> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
};

const CHECKIN_EARLY_MINUTES = 60;
const MINUTE_IN_MILLISECONDS = 60 * 1000;

function canCheckInAppointment(appointment: AppointmentResponse, currentTimeMillis: number) {
  const startAtMillis = new Date(appointment.startAt).getTime();
  const endAtMillis = new Date(appointment.endAt).getTime();
  const checkInOpenAtMillis = startAtMillis - CHECKIN_EARLY_MINUTES * MINUTE_IN_MILLISECONDS;

  return appointment.status === "CONFIRMED"
    && currentTimeMillis >= checkInOpenAtMillis
    && currentTimeMillis <= endAtMillis;
}

function canCompleteAppointment(appointment: AppointmentResponse, currentTimeMillis: number) {
  const startAtMillis = new Date(appointment.startAt).getTime();

  return appointment.status === "CHECKED_IN"
    && currentTimeMillis >= startAtMillis;
}

function getAppointmentStatusLabel(appointment: AppointmentResponse, currentTimeMillis: number) {
  if (canCompleteAppointment(appointment, currentTimeMillis)) {
    return "Em atendimento";
  }

  return appointmentStatusLabels[appointment.status];
}

export function AppointmentsPage() {
  const [date, setDate] = useState(toDateInputValue());
  const [professionalId, setProfessionalId] = useState("");
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerResponse | null>(null);
  const [selectedFormCustomer, setSelectedFormCustomer] = useState<CustomerResponse | null>(null);
  const [serviceId, setServiceId] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState<PageResponse<AppointmentResponse>>(emptyPage);
  const [professionals, setProfessionals] = useState<ProfessionalResponse[]>([]);
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [operation, setOperation] = useState<OperationState | null>(null);
  const [operationStartAt, setOperationStartAt] = useState("");
  const [operationReason, setOperationReason] = useState("");
  const [currentTimeMillis, setCurrentTimeMillis] = useState(() => Date.now());
  const { register, handleSubmit, reset, setValue, formState: { errors, isSubmitting } } = useForm<AppointmentFormValues>({
    defaultValues: {
      customerId: "",
      professionalId: "",
      serviceId: "",
      startAt: ""
    }
  });

  useEffect(() => {
    Promise.all([
      listProfessionals({ active: true, size: 100 }),
      listServices({ active: true, size: 100 })
    ])
      .then(([professionalPage, servicePage]) => {
        setProfessionals(professionalPage.content);
        setServices(servicePage.content);
      })
      .catch((caught) => setError(getApiErrorMessage(caught)));
  }, []);

  useEffect(() => {
    const intervalId = window.setInterval(() => setCurrentTimeMillis(Date.now()), MINUTE_IN_MILLISECONDS);
    return () => window.clearInterval(intervalId);
  }, []);

  useEffect(() => {
    loadAppointments(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date, professionalId, selectedCustomer, serviceId, status]);

  async function loadAppointments(nextPage = page.page) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listAppointments({
        date,
        professionalId: professionalId ? Number(professionalId) : "",
        customerId: selectedCustomer ? selectedCustomer.id : "",
        serviceId: serviceId ? Number(serviceId) : "",
        status: status as AppointmentStatus | "",
        page: nextPage,
        size: 10,
        sort: "startAt,asc"
      });
      setPage(response);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    } finally {
      setIsLoading(false);
    }
  }

  async function onCreate(values: AppointmentFormValues) {
    setMessage(null);
    setError(null);
    try {
      await createAppointment({
        customerId: Number(values.customerId),
        professionalId: Number(values.professionalId),
        serviceId: Number(values.serviceId),
        startAt: localDateTimeToInstant(values.startAt)
      });
      setMessage("Agendamento criado com sucesso.");
      setSelectedFormCustomer(null);
      reset({ customerId: "", professionalId: "", serviceId: "", startAt: "" });
      await loadAppointments(0);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  function openOperation(type: OperationType, appointment: AppointmentResponse) {
    setOperation({ type, appointment });
    setOperationReason("");
    setOperationStartAt(type === "reschedule" ? toLocalDateTimeInputValue(appointment.startAt) : "");
  }

  async function submitOperation() {
    if (!operation) {
      return;
    }
    setMessage(null);
    setError(null);
    try {
      if (operation.type === "reschedule") {
        await rescheduleAppointment(operation.appointment.id, {
          startAt: localDateTimeToInstant(operationStartAt)
        });
        setMessage("Agendamento remarcado com sucesso.");
      }
      if (operation.type === "cancel") {
        await cancelAppointment(operation.appointment.id, { cancelReason: operationReason });
        setMessage("Agendamento cancelado com sucesso.");
      }
      if (operation.type === "noShow") {
        await markAppointmentNoShow(operation.appointment.id, { noShowReason: operationReason || null });
        setMessage("Falta registrada com sucesso.");
      }
      setOperation(null);
      await loadAppointments(page.page);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  async function runSimpleAction(action: "confirm" | "checkIn" | "complete", appointment: AppointmentResponse) {
    const labels = {
      confirm: "confirmado",
      checkIn: "com check-in registrado",
      complete: "concluído"
    };
    setMessage(null);
    setError(null);
    try {
      if (action === "confirm") {
        await confirmAppointment(appointment.id);
      }
      if (action === "checkIn") {
        await checkInAppointment(appointment.id);
      }
      if (action === "complete") {
        await completeAppointment(appointment.id);
      }
      setMessage(`Agendamento ${labels[action]} com sucesso.`);
      await loadAppointments(page.page);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  const hasOptions = useMemo(
    () => professionals.length > 0 && services.length > 0,
    [professionals.length, services.length]
  );

  return (
    <section>
      <PageHeader
        title="Agenda diária"
        description="Listagem paginada com filtros, criação de agendamentos e ações operacionais disponíveis."
        actions={<button type="button" className="button-secondary" onClick={() => loadAppointments(page.page)}><RefreshCw size={16} /> Atualizar</button>}
      />

      <div className="filter-bar">
        <label>
          Data
          <input type="date" value={date} onChange={(event) => setDate(event.target.value)} />
        </label>
        <label>
          Profissional
          <select value={professionalId} onChange={(event) => setProfessionalId(event.target.value)}>
            <option value="">Todos</option>
            {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.name}</option>)}
          </select>
        </label>
        <label>
          Cliente
          <AsyncClientSelect
            value={selectedCustomer}
            onChange={setSelectedCustomer}
            placeholder="Todos os clientes"
          />
        </label>
        <label>
          Serviço
          <select value={serviceId} onChange={(event) => setServiceId(event.target.value)}>
            <option value="">Todos</option>
            {services.map((service) => <option key={service.id} value={service.id}>{service.name}</option>)}
          </select>
        </label>
        <label>
          Status
          <select value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="">Todos</option>
            {appointmentStatusOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
          </select>
        </label>
      </div>

      {message ? <Feedback type="success">{message}</Feedback> : null}
      {error ? <Feedback type="error">{error}</Feedback> : null}

      <div className="split-layout">
        <div className="content-panel">
          {isLoading ? <LoadingState /> : null}
          {!isLoading && page.content.length === 0 ? <EmptyState title="Nenhum agendamento encontrado." description="Ajuste os filtros ou crie um novo agendamento." /> : null}

          {page.content.length > 0 ? (
            <>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Horário</th>
                      <th>Cliente</th>
                      <th>Profissional</th>
                      <th>Serviço</th>
                      <th>Status</th>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {page.content.map((appointment) => (
                      <tr key={appointment.id}>
                        <td>
                          <strong>{formatDateTime(appointment.startAt)}</strong>
                          <span className="muted-line">até {formatDateTime(appointment.endAt)}</span>
                        </td>
                        <td>{appointment.customerName}</td>
                        <td>{appointment.professionalName}</td>
                        <td>{appointment.serviceName}</td>
                        <td>
                          <StatusBadge
                            status={appointment.status}
                            label={getAppointmentStatusLabel(appointment, currentTimeMillis)}
                          />
                        </td>
                        <td className="table-actions">
                          {appointment.status === "SCHEDULED" ? (
                            <button type="button" className="button-secondary" onClick={() => runSimpleAction("confirm", appointment)}>Confirmar</button>
                          ) : null}
                          {canCheckInAppointment(appointment, currentTimeMillis) ? (
                            <button type="button" className="button-secondary" onClick={() => runSimpleAction("checkIn", appointment)}>Check-in</button>
                          ) : null}
                          {canCompleteAppointment(appointment, currentTimeMillis) ? (
                            <button type="button" className="button-secondary" onClick={() => runSimpleAction("complete", appointment)}>Concluir</button>
                          ) : null}
                          {appointment.status === "SCHEDULED" || appointment.status === "CONFIRMED" ? (
                            <>
                              <button type="button" className="button-secondary" onClick={() => openOperation("reschedule", appointment)}>Remarcar</button>
                              <button type="button" className="button-danger" onClick={() => openOperation("cancel", appointment)}>Cancelar</button>
                              <button type="button" className="button-secondary" onClick={() => openOperation("noShow", appointment)}>Falta</button>
                            </>
                          ) : null}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} onPageChange={loadAppointments} />
            </>
          ) : null}
        </div>

        <aside className="content-panel form-panel">
          <h2>Novo agendamento</h2>
          {!hasOptions ? <Feedback type="warning">Cadastre profissional e serviço ativos antes de agendar.</Feedback> : null}
          <form className="form-stack" onSubmit={handleSubmit(onCreate)}>
            <label>
              Cliente
              <AsyncClientSelect
                value={selectedFormCustomer}
                onChange={(customer) => {
                  setSelectedFormCustomer(customer);
                  setValue("customerId", customer ? String(customer.id) : "", {
                    shouldDirty: true,
                    shouldValidate: true
                  });
                }}
                placeholder="Digite nome, telefone ou e-mail"
                disabled={!hasOptions}
                required
              />
              <input type="hidden" {...register("customerId", { required: "Selecione o cliente." })} />
              {errors.customerId ? <small className="field-error">{errors.customerId.message}</small> : null}
            </label>
            <label>
              Profissional
              <select {...register("professionalId", { required: "Selecione o profissional." })}>
                <option value="">Selecione</option>
                {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.name}</option>)}
              </select>
              {errors.professionalId ? <small className="field-error">{errors.professionalId.message}</small> : null}
            </label>
            <label>
              Serviço
              <select {...register("serviceId", { required: "Selecione o serviço." })}>
                <option value="">Selecione</option>
                {services.map((service) => <option key={service.id} value={service.id}>{service.name}</option>)}
              </select>
              {errors.serviceId ? <small className="field-error">{errors.serviceId.message}</small> : null}
            </label>
            <label>
              Início
              <input type="datetime-local" {...register("startAt", { required: "Informe o horário de início." })} />
              {errors.startAt ? <small className="field-error">{errors.startAt.message}</small> : null}
            </label>
            <button type="submit" className="button-primary" disabled={isSubmitting || !hasOptions}>
              <CalendarPlus size={16} /> {isSubmitting ? "Agendando..." : "Criar agendamento"}
            </button>
          </form>
        </aside>
      </div>

      {operation ? (
        <div className="operation-panel" role="dialog" aria-modal="true" aria-label="Ação do agendamento">
          <div className="content-panel">
            <h2>
              {operation.type === "reschedule" ? "Remarcar agendamento" : null}
              {operation.type === "cancel" ? "Cancelar agendamento" : null}
              {operation.type === "noShow" ? "Registrar falta" : null}
            </h2>
            <p>{operation.appointment.customerName} · {formatDateTime(operation.appointment.startAt)}</p>
            <div className="form-stack">
              {operation.type === "reschedule" ? (
                <label>
                  Novo início
                  <input type="datetime-local" value={operationStartAt} onChange={(event) => setOperationStartAt(event.target.value)} />
                </label>
              ) : null}
              {operation.type === "cancel" ? (
                <label>
                  Motivo do cancelamento
                  <textarea rows={3} value={operationReason} onChange={(event) => setOperationReason(event.target.value)} />
                </label>
              ) : null}
              {operation.type === "noShow" ? (
                <label>
                  Motivo da falta
                  <textarea rows={3} value={operationReason} onChange={(event) => setOperationReason(event.target.value)} />
                </label>
              ) : null}
              <div className="modal-actions">
                <button type="button" className="button-secondary" onClick={() => setOperation(null)}>Fechar</button>
                <button type="button" className="button-primary" onClick={submitOperation}>
                  <Save size={16} /> Confirmar
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
