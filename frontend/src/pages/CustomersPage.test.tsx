import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createCustomer,
  deactivateCustomer,
  listCustomers,
  updateCustomer
} from "../services/customerService";
import { customerResponse, pageResponse } from "../test/factories";
import { renderWithProviders } from "../test/render";
import { CustomersPage } from "./CustomersPage";

vi.mock("../services/customerService", () => ({
  createCustomer: vi.fn(),
  deactivateCustomer: vi.fn(),
  listCustomers: vi.fn(),
  updateCustomer: vi.fn()
}));

const mockedCreateCustomer = vi.mocked(createCustomer);
const mockedDeactivateCustomer = vi.mocked(deactivateCustomer);
const mockedListCustomers = vi.mocked(listCustomers);
const mockedUpdateCustomer = vi.mocked(updateCustomer);

describe("CustomersPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedListCustomers.mockResolvedValue(pageResponse([customerResponse()]));
    mockedCreateCustomer.mockResolvedValue(customerResponse({ id: 2, name: "Cliente Novo" }));
    mockedUpdateCustomer.mockResolvedValue(customerResponse({ name: "Maria Atualizada" }));
    mockedDeactivateCustomer.mockResolvedValue();
  });

  it("lists customers and applies search filters", async () => {
    renderWithProviders(<CustomersPage />);

    expect(await screen.findByText("Maria Cliente")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText(/busca/i), { target: { value: "Maria" } });
    fireEvent.change(screen.getByLabelText(/situação/i), { target: { value: "true" } });

    await waitFor(() => {
      expect(mockedListCustomers).toHaveBeenCalledWith(expect.objectContaining({
        search: "Maria",
        active: true,
        page: 0,
        size: 10
      }));
    });
  });

  it("creates a customer with normalized optional empty fields", async () => {
    renderWithProviders(<CustomersPage />);

    await screen.findByText("Maria Cliente");
    const formPanel = screen.getByRole("heading", { name: "Novo cliente" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Nome$/i), { target: { value: "Cliente Novo" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Telefone$/i), { target: { value: "11977770000" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /salvar cliente/i }));

    await waitFor(() => {
      expect(mockedCreateCustomer).toHaveBeenCalledWith({
        name: "Cliente Novo",
        phone: "11977770000",
        email: null,
        document: null
      });
    });
    expect(await screen.findByText("Cliente criado com sucesso.")).toBeInTheDocument();
  });

  it("updates and deactivates customers from the list", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);

    renderWithProviders(<CustomersPage />);

    await screen.findByText("Maria Cliente");
    fireEvent.click(screen.getByRole("button", { name: /editar/i }));
    const formPanel = screen.getByRole("heading", { name: "Editar cliente" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Nome$/i), { target: { value: "Maria Atualizada" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /salvar cliente/i }));

    await waitFor(() => {
      expect(mockedUpdateCustomer).toHaveBeenCalledWith(1, expect.objectContaining({
        name: "Maria Atualizada",
        active: true
      }));
    });

    fireEvent.click(screen.getByRole("button", { name: /inativar/i }));

    await waitFor(() => expect(mockedDeactivateCustomer).toHaveBeenCalledWith(1));
  });
});
