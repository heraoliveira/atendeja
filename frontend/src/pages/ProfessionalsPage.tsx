import { useEffect, useState } from "react";
import { Plus, Save } from "lucide-react";
import { useForm } from "react-hook-form";
import { EmptyState, Feedback, LoadingState } from "../components/Feedback";
import { PageHeader } from "../components/PageHeader";
import { Pagination } from "../components/Pagination";
import { useAuth } from "../contexts/AuthContext";
import {
  createProfessional,
  deactivateProfessional,
  listProfessionals,
  updateProfessional
} from "../services/professionalService";
import type { PageResponse, ProfessionalResponse, ProfessionalUpdateRequest } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";

type ProfessionalFormValues = Omit<ProfessionalUpdateRequest, "active"> & { active: string | boolean };

function normalizeActive(value: ProfessionalFormValues["active"]): boolean {
  return value === true || String(value) === "true";
}

const emptyPage: PageResponse<ProfessionalResponse> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
};

export function ProfessionalsPage() {
  const { hasRole } = useAuth();
  const canManage = hasRole(["ADMIN"]);
  const [page, setPage] = useState<PageResponse<ProfessionalResponse>>(emptyPage);
  const [search, setSearch] = useState("");
  const [active, setActive] = useState("");
  const [selected, setSelected] = useState<ProfessionalResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<ProfessionalFormValues>({
    defaultValues: { name: "", phone: "", email: "", active: "true" }
  });

  useEffect(() => {
    loadProfessionals(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, active]);

  async function loadProfessionals(nextPage = page.page) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listProfessionals({
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
    reset({ name: "", phone: "", email: "", active: "true" });
  }

  function startEdit(professional: ProfessionalResponse) {
    setSelected(professional);
    reset({
      name: professional.name,
      phone: professional.phone,
      email: professional.email ?? "",
      active: professional.active ? "true" : "false"
    });
  }

  async function onSubmit(values: ProfessionalFormValues) {
    setMessage(null);
    setError(null);
    try {
      if (selected) {
        await updateProfessional(selected.id, {
          ...values,
          active: normalizeActive(values.active),
          email: values.email || null
        });
        setMessage("Profissional atualizado com sucesso.");
      } else {
        await createProfessional({
          name: values.name,
          phone: values.phone,
          email: values.email || null
        });
        setMessage("Profissional criado com sucesso.");
        startCreate();
      }
      await loadProfessionals(selected ? page.page : 0);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  async function handleDeactivate(professional: ProfessionalResponse) {
    if (!window.confirm(`Inativar o profissional ${professional.name}?`)) {
      return;
    }
    setMessage(null);
    setError(null);
    try {
      await deactivateProfessional(professional.id);
      setMessage("Profissional inativado com sucesso.");
      await loadProfessionals(page.page);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  return (
    <section>
      <PageHeader
        title="Profissionais"
        description="Prestadores com agenda, disponibilidade e indicadores operacionais."
        actions={canManage ? <button type="button" className="button-primary" onClick={startCreate}><Plus size={16} /> Novo profissional</button> : null}
      />

      {!canManage ? <Feedback type="warning">Seu perfil pode consultar profissionais, mas não pode criar, editar ou inativar registros.</Feedback> : null}

      <div className="split-layout">
        <div className="content-panel">
          <div className="filter-bar compact">
            <label>
              Busca
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Nome, telefone ou e-mail" />
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
          {!isLoading && page.content.length === 0 ? <EmptyState title="Nenhum profissional encontrado." /> : null}

          {page.content.length > 0 ? (
            <>
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Nome</th>
                      <th>Telefone</th>
                      <th>E-mail</th>
                      <th>Situação</th>
                      {canManage ? <th>Ações</th> : null}
                    </tr>
                  </thead>
                  <tbody>
                    {page.content.map((professional) => (
                      <tr key={professional.id}>
                        <td>{professional.name}</td>
                        <td>{professional.phone}</td>
                        <td>{professional.email ?? "-"}</td>
                        <td>{professional.active ? "Ativo" : "Inativo"}</td>
                        {canManage ? (
                          <td className="table-actions">
                            <button type="button" className="button-secondary" onClick={() => startEdit(professional)}>Editar</button>
                            {professional.active ? (
                              <button type="button" className="button-danger" onClick={() => handleDeactivate(professional)}>Inativar</button>
                            ) : null}
                          </td>
                        ) : null}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} onPageChange={loadProfessionals} />
            </>
          ) : null}
        </div>

        {canManage ? (
          <aside className="content-panel form-panel">
            <h2>{selected ? "Editar profissional" : "Novo profissional"}</h2>
            {message ? <Feedback type="success">{message}</Feedback> : null}
            <form className="form-stack" onSubmit={handleSubmit(onSubmit)}>
              <label>
                Nome
                <input {...register("name", { required: "Informe o nome." })} />
                {errors.name ? <small className="field-error">{errors.name.message}</small> : null}
              </label>
              <label>
                Telefone
                <input {...register("phone", { required: "Informe o telefone." })} />
                {errors.phone ? <small className="field-error">{errors.phone.message}</small> : null}
              </label>
              <label>
                E-mail
                <input type="email" {...register("email")} />
              </label>
              {selected ? (
                <label>
                  Situação
                  <select {...register("active", { setValueAs: (value) => value === true || value === "true" })}>
                    <option value="true">Ativo</option>
                    <option value="false">Inativo</option>
                  </select>
                </label>
              ) : null}
              <button type="submit" className="button-primary" disabled={isSubmitting}>
                <Save size={16} /> {isSubmitting ? "Salvando..." : "Salvar profissional"}
              </button>
            </form>
          </aside>
        ) : null}
      </div>
    </section>
  );
}
