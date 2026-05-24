import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  createProfessional,
  deactivateProfessional,
  listProfessionals,
  updateProfessional
} from "../services/professionalService";
import { pageResponse, professionalResponse } from "../test/factories";
import { renderWithProviders } from "../test/render";
import { ProfessionalsPage } from "./ProfessionalsPage";

vi.mock("../services/professionalService", () => ({
  createProfessional: vi.fn(),
  deactivateProfessional: vi.fn(),
  listProfessionals: vi.fn(),
  updateProfessional: vi.fn()
}));

const mockedCreateProfessional = vi.mocked(createProfessional);
const mockedDeactivateProfessional = vi.mocked(deactivateProfessional);
const mockedListProfessionals = vi.mocked(listProfessionals);
const mockedUpdateProfessional = vi.mocked(updateProfessional);

describe("ProfessionalsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockedListProfessionals.mockResolvedValue(pageResponse([professionalResponse()]));
    mockedCreateProfessional.mockResolvedValue(professionalResponse({ id: 4, name: "Novo Profissional" }));
    mockedUpdateProfessional.mockResolvedValue(professionalResponse({ name: "Ana Atualizada" }));
    mockedDeactivateProfessional.mockResolvedValue();
  });

  it("allows admins to create update and deactivate professionals", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);

    renderWithProviders(<ProfessionalsPage />, { role: "ADMIN" });

    expect(await screen.findByText("Ana Profissional")).toBeInTheDocument();
    const formPanel = screen.getByRole("heading", { name: "Novo profissional" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Nome$/i), { target: { value: "Novo Profissional" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Telefone$/i), { target: { value: "11977770000" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /salvar profissional/i }));

    await waitFor(() => {
      expect(mockedCreateProfessional).toHaveBeenCalledWith({
        name: "Novo Profissional",
        phone: "11977770000",
        email: null
      });
    });

    fireEvent.click(screen.getByRole("button", { name: /editar/i }));
    const editPanel = screen.getByRole("heading", { name: "Editar profissional" }).closest("aside") as HTMLElement;
    fireEvent.change(within(editPanel).getByLabelText(/^Nome$/i), { target: { value: "Ana Atualizada" } });
    fireEvent.click(within(editPanel).getByRole("button", { name: /salvar profissional/i }));

    await waitFor(() => {
      expect(mockedUpdateProfessional).toHaveBeenCalledWith(2, expect.objectContaining({
        name: "Ana Atualizada",
        active: true
      }));
    });

    fireEvent.click(screen.getByRole("button", { name: /inativar/i }));

    await waitFor(() => expect(mockedDeactivateProfessional).toHaveBeenCalledWith(2));
  });

  it("hides administrative professional actions for attendants", async () => {
    renderWithProviders(<ProfessionalsPage />, { role: "ATTENDANT" });

    expect(await screen.findByText("Ana Profissional")).toBeInTheDocument();
    expect(screen.getByText(/seu perfil pode consultar profissionais/i)).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /novo profissional/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /editar/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: /inativar/i })).not.toBeInTheDocument();
  });
});
