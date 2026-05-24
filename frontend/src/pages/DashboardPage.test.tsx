import { fireEvent, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getDailyDashboard } from "../services/dashboardService";
import { listProfessionals } from "../services/professionalService";
import { dailyDashboardResponse, pageResponse, professionalResponse } from "../test/factories";
import { renderWithProviders } from "../test/render";
import { DashboardPage } from "./DashboardPage";

vi.mock("../services/dashboardService", () => ({
  getDailyDashboard: vi.fn()
}));

vi.mock("../services/professionalService", () => ({
  listProfessionals: vi.fn()
}));

const mockedGetDailyDashboard = vi.mocked(getDailyDashboard);
const mockedListProfessionals = vi.mocked(listProfessionals);

describe("DashboardPage", () => {
  beforeEach(() => {
    mockedGetDailyDashboard.mockReset();
    mockedListProfessionals.mockReset();
    mockedListProfessionals.mockResolvedValue(pageResponse([
      professionalResponse({ id: 2, name: "Ana Profissional" })
    ]));
    mockedGetDailyDashboard.mockResolvedValue(dailyDashboardResponse());
  });

  it("loads daily metrics and renders operational indicators", async () => {
    renderWithProviders(<DashboardPage />);

    expect(await screen.findByText("Dashboard diário")).toBeInTheDocument();
    expect(await screen.findByText("Agendamentos")).toBeInTheDocument();
    expect(screen.getByText("Cancelamentos")).toBeInTheDocument();
    expect(screen.getByText("Faltas")).toBeInTheDocument();
    expect(screen.getByText("62.50%")).toBeInTheDocument();
    expect(screen.getByText("8 h")).toBeInTheDocument();
    expect(screen.getByText("5 h")).toBeInTheDocument();
  });

  it("reloads dashboard with selected date and professional filter", async () => {
    renderWithProviders(<DashboardPage />);

    await screen.findByText("Ana Profissional");
    fireEvent.change(screen.getByLabelText(/data/i), { target: { value: "2030-01-21" } });
    fireEvent.change(screen.getByLabelText(/profissional/i), { target: { value: "2" } });

    await waitFor(() => {
      expect(mockedGetDailyDashboard).toHaveBeenLastCalledWith({
        date: "2030-01-21",
        professionalId: 2
      });
    });
  });
});
