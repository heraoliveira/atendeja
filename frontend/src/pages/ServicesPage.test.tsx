import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createService,
  deactivateService,
  listServices,
  updateService
} from "../services/serviceCatalogService";
import { pageResponse, serviceResponse } from "../test/factories";
import { renderWithProviders } from "../test/render";
import { ServicesPage } from "./ServicesPage";

vi.mock("../services/serviceCatalogService", () => ({
  createService: vi.fn(),
  deactivateService: vi.fn(),
  listServices: vi.fn(),
  updateService: vi.fn()
}));

const mockedCreateService = vi.mocked(createService);
const mockedDeactivateService = vi.mocked(deactivateService);
const mockedListServices = vi.mocked(listServices);
const mockedUpdateService = vi.mocked(updateService);

describe("ServicesPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedListServices.mockResolvedValue(pageResponse([serviceResponse()]));
    mockedCreateService.mockResolvedValue(serviceResponse({ id: 4, name: "Novo serviço" }));
    mockedUpdateService.mockResolvedValue(serviceResponse({ name: "Consulta atualizada" }));
    mockedDeactivateService.mockResolvedValue();
  });

  it("allows admins to create update and deactivate services", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);

    renderWithProviders(<ServicesPage />, { role: "ADMIN" });

    expect(await screen.findByText("Consulta inicial")).toBeInTheDocument();
    const formPanel = screen.getByRole("heading", { name: "Novo serviço" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Nome$/i), { target: { value: "Novo serviço" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Descrição$/i), { target: { value: "" } });
    fireEvent.change(within(formPanel).getByLabelText(/duração em minutos/i), { target: { value: "60" } });
    fireEvent.change(within(formPanel).getByLabelText(/intervalo em minutos/i), { target: { value: "10" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Preço$/i), { target: { value: "200.50" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /salvar serviço/i }));

    await waitFor(() => {
      expect(mockedCreateService).toHaveBeenCalledWith({
        name: "Novo serviço",
        description: null,
        durationMinutes: 60,
        bufferMinutes: 10,
        price: 200.5
      });
    });

    fireEvent.click(screen.getByRole("button", { name: /editar/i }));
    const editPanel = screen.getByRole("heading", { name: "Editar serviço" }).closest("aside") as HTMLElement;
    fireEvent.change(within(editPanel).getByLabelText(/^Nome$/i), { target: { value: "Consulta atualizada" } });
    fireEvent.click(within(editPanel).getByRole("button", { name: /salvar serviço/i }));

    await waitFor(() => {
      expect(mockedUpdateService).toHaveBeenCalledWith(3, expect.objectContaining({
        name: "Consulta atualizada",
        durationMinutes: 45,
        bufferMinutes: 15,
        price: 150,
        active: true
      }));
    });

    fireEvent.click(screen.getByRole("button", { name: /inativar/i }));

    await waitFor(() => expect(mockedDeactivateService).toHaveBeenCalledWith(3));
  });

  it("hides administrative service actions for attendants", async () => {
    renderWithProviders(<ServicesPage />, { role: "ATTENDANT" });

    expect(await screen.findByText("Consulta inicial")).toBeInTheDocument();
    expect(screen.getByText(/seu perfil pode consultar serviços/i)).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /novo serviço/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /editar/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /inativar/i })).not.toBeInTheDocument();
  });
});
