import { useEffect, useState } from "react";
import { Plus, Save } from "lucide-react";
import { useForm } from "react-hook-form";
import { EmptyState, Feedback, LoadingState } from "../components/Feedback";
import { PageHeader } from "../components/PageHeader";
import { Pagination } from "../components/Pagination";
import { useAuth } from "../contexts/AuthContext";
import {
  createService,
  deactivateService,
  listServices,
  updateService
} from "../services/serviceCatalogService";
import type { PageResponse, ServiceResponse, ServiceUpdateRequest } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";
import { formatCurrency } from "../utils/formatters";

type ServiceFormValues = ServiceUpdateRequest;

const emptyPage: PageResponse<ServiceResponse> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
};

export function ServicesPage() {
  const { hasRole } = useAuth();
  const canManage = hasRole(["ADMIN"]);
  const [page, setPage] = useState<PageResponse<ServiceResponse>>(emptyPage);
  const [search, setSearch] = useState("");
  const [active, setActive] = useState("");
  const [selected, setSelected] = useState<ServiceResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<ServiceFormValues>({
    defaultValues: {
      name: "",
      description: "",
      durationMinutes: 30,
      bufferMinutes: 0,
      price: 0,
      active: true
    }
  });

  useEffect(() => {
    loadServices(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, active]);

  async function loadServices(nextPage = page.page) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listServices({
        search,
        active: active === "" ? "" : active === "true",
        page: nextPage,
        size: 10
      });
      setPage(response);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    } finally {
      setIsLoading(false);
    }
  }

  function startCreate() {
    setSelected(null);
    reset({ name: "", description: "", durationMinutes: 30, bufferMinutes: 0, price: 0, active: true });
  }

  function startEdit(service: ServiceResponse) {
    setSelected(service);
    reset({
      name: service.name,
      description: service.description ?? "",
      durationMinutes: service.durationMinutes,
      bufferMinutes: service.bufferMinutes,
      price: Number(service.price),
      active: service.active
    });
  }

  async function onSubmit(values: ServiceFormValues) {
    setMessage(null);
    setError(null);
    try {
      const normalized = {
        ...values,
        description: values.description || null,
        durationMinutes: Number(values.durationMinutes),
        bufferMinutes: Number(values.bufferMinutes),
        price: Number(values.price)
      };
      if (selected) {
        await updateService(selected.id, normalized);
        setMessage("Serviço atualizado com sucesso.");
      } else {
        await createService({
          name: normalized.name,
          description: normalized.description,
          durationMinutes: normalized.durationMinutes,
          bufferMinutes: normalized.bufferMinutes,
          price: normalized.price
        });
        setMessage("Serviço criado com sucesso.");
        startCreate();
      }
      await loadServices(selected ? page.page : 0);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  async function handleDeactivate(service: ServiceResponse) {
    if (!window.confirm(`Inativar o serviço ${service.name}?`)) {
      return;
    }
    setMessage(null);
    setError(null);
    try {
      await deactivateService(service.id);
      setMessage("Serviço inativado com sucesso.");
      await loadServices(page.page);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  return (
    <section>
      <PageHeader
        title="Serviços"
        description="Catálogo com duração, intervalo e preço usados no cálculo dos agendamentos."
        actions={canManage ? <button type="button" className="button-primary" onClick={startCreate}><Plus size={16} /> Novo serviço</button> : null}
      />

      {!canManage ? <Feedback type="warning">Seu perfil pode consultar serviços, mas não pode criar, editar ou inativar registros.</Feedback> : null}

      <div className="split-layout">
        <div className="content-panel">
          <div className="filter-bar compact">
            <label>
              Busca
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome ou descrição" />
            </label>
            <label>
              Situação
              <select value={active} onChange={(event) => setActive(event.target.value)}>
                <option value="">Todas</option>
                <option value="true">Ativos</option>
                <option value="false">Inativos</option>
              </select>
            </label>
          </div>

          {isLoading ? <LoadingState /> : null}
          {error ? <Feedback type="error">{error}</Feedback> : null}
          {!isLoading && page.content.length === 0 ? <EmptyState title="Nenhum serviço encontrado." /> : null}

          {page.content.length > 0 ? (
            <>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>Duração</th>
                      <th>Intervalo</th>
                      <th>Preço</th>
                      <th>Situação</th>
                      {canManage ? <th>Ações</th> : null}
                    </tr>
                  </thead>
                  <tbody>
                    {page.content.map((service) => (
                      <tr key={service.id}>
                        <td>{service.name}</td>
                        <td>{service.durationMinutes} min</td>
                        <td>{service.bufferMinutes} min</td>
                        <td>{formatCurrency(service.price)}</td>
                        <td>{service.active ? "Ativo" : "Inativo"}</td>
                        {canManage ? (
                          <td className="table-actions">
                            <button type="button" className="button-secondary" onClick={() => startEdit(service)}>Editar</button>
                            {service.active ? (
                              <button type="button" className="button-danger" onClick={() => handleDeactivate(service)}>Inativar</button>
                            ) : null}
                          </td>
                        ) : null}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} onPageChange={loadServices} />
            </>
          ) : null}
        </div>

        {canManage ? (
          <aside className="content-panel form-panel">
            <h2>{selected ? "Editar serviço" : "Novo serviço"}</h2>
            {message ? <Feedback type="success">{message}</Feedback> : null}
            <form className="form-stack" onSubmit={handleSubmit(onSubmit)}>
              <label>
                Nome
                <input {...register("name", { required: "Informe o nome." })} />
                {errors.name ? <small className="field-error">{errors.name.message}</small> : null}
              </label>
              <label>
                Descrição
                <textarea rows={3} {...register("description")} />
              </label>
              <div className="form-grid">
                <label>
                  Duração em minutos
                  <input type="number" min={1} {...register("durationMinutes", { valueAsNumber: true, min: 1 })} />
                </label>
                <label>
                  Intervalo em minutos
                  <input type="number" min={0} {...register("bufferMinutes", { valueAsNumber: true, min: 0 })} />
                </label>
              </div>
              <label>
                Preço
                <input type="number" min={0} step="0.01" {...register("price", { valueAsNumber: true, min: 0 })} />
              </label>
              {selected ? (
                <label>
                  Situação
                  <select {...register("active", { setValueAs: (value) => value === "true" })}>
                    <option value="true">Ativo</option>
                    <option value="false">Inativo</option>
                  </select>
                </label>
              ) : null}
              <button type="submit" className="button-primary" disabled={isSubmitting}>
                <Save size={16} /> {isSubmitting ? "Salvando..." : "Salvar serviço"}
              </button>
            </form>
          </aside>
        ) : null}
      </div>
    </section>
  );
}
