import { useEffect, useState } from "react";
import { Plus, Save } from "lucide-react";
import { useForm } from "react-hook-form";
import { EmptyState, Feedback, LoadingState } from "../components/Feedback";
import { PageHeader } from "../components/PageHeader";
import { Pagination } from "../components/Pagination";
import {
  createCustomer,
  deactivateCustomer,
  listCustomers,
  updateCustomer
} from "../services/customerService";
import type { CustomerResponse, CustomerUpdateRequest, PageResponse } from "../types/api";
import { getApiErrorMessage } from "../utils/errors";

type CustomerFormValues = Omit<CustomerUpdateRequest, "active"> & { active: string | boolean };

function normalizeActive(value: CustomerFormValues["active"]): boolean {
  return value === true || String(value) === "true";
}

const emptyPage: PageResponse<CustomerResponse> = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true
};

export function CustomersPage() {
  const [page, setPage] = useState<PageResponse<CustomerResponse>>(emptyPage);
  const [search, setSearch] = useState("");
  const [active, setActive] = useState("");
  const [selected, setSelected] = useState<CustomerResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<CustomerFormValues>({
    defaultValues: { name: "", phone: "", email: "", document: "", active: "true" }
  });

  useEffect(() => {
    loadCustomers(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, active]);

  async function loadCustomers(nextPage = page.page) {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listCustomers({
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
    reset({ name: "", phone: "", email: "", document: "", active: "true" });
  }

  function startEdit(customer: CustomerResponse) {
    setSelected(customer);
    reset({
      name: customer.name,
      phone: customer.phone,
      email: customer.email ?? "",
      document: customer.document ?? "",
      active: customer.active ? "true" : "false"
    });
  }

  async function onSubmit(values: CustomerFormValues) {
    setMessage(null);
    setError(null);
    try {
      if (selected) {
        await updateCustomer(selected.id, {
          ...values,
          active: normalizeActive(values.active),
          email: values.email || null,
          document: values.document || null
        });
        setMessage("Cliente atualizado com sucesso.");
      } else {
        await createCustomer({
          name: values.name,
          phone: values.phone,
          email: values.email || null,
          document: values.document || null
        });
        setMessage("Cliente criado com sucesso.");
        startCreate();
      }
      await loadCustomers(selected ? page.page : 0);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  async function handleDeactivate(customer: CustomerResponse) {
    if (!window.confirm(`Inativar o cliente ${customer.name}?`)) {
      return;
    }
    setMessage(null);
    setError(null);
    try {
      await deactivateCustomer(customer.id);
      setMessage("Cliente inativado com sucesso.");
      await loadCustomers(page.page);
    } catch (caught) {
      setError(getApiErrorMessage(caught));
    }
  }

  return (
    <section>
      <PageHeader
        title="Clientes"
        description="Cadastro de pessoas atendidas e busca para agendamentos."
        actions={<button type="button" className="button-primary" onClick={startCreate}><Plus size={16} /> Novo cliente</button>}
      />

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
          {!isLoading && page.content.length === 0 ? <EmptyState title="Nenhum cliente encontrado." /> : null}

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
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {page.content.map((customer) => (
                      <tr key={customer.id}>
                        <td>{customer.name}</td>
                        <td>{customer.phone}</td>
                        <td>{customer.email ?? "-"}</td>
                        <td>{customer.active ? "Ativo" : "Inativo"}</td>
                        <td className="table-actions">
                          <button type="button" className="button-secondary" onClick={() => startEdit(customer)}>Editar</button>
                          {customer.active ? (
                            <button type="button" className="button-danger" onClick={() => handleDeactivate(customer)}>Inativar</button>
                          ) : null}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <Pagination page={page} onPageChange={loadCustomers} />
            </>
          ) : null}
        </div>

        <aside className="content-panel form-panel">
          <h2>{selected ? "Editar cliente" : "Novo cliente"}</h2>
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
            <label>
              Documento
              <input {...register("document")} />
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
              <Save size={16} /> {isSubmitting ? "Salvando..." : "Salvar cliente"}
            </button>
          </form>
        </aside>
      </div>
    </section>
  );
}
