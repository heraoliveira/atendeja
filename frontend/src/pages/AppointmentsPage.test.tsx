import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  cancelAppointment,
  confirmAppointment,
  createAppointment,
  listAppointments,
  rescheduleAppointment
} from "../services/appointmentService";
import { listCustomers } from "../services/customerService";
import { listProfessionals } from "../services/professionalService";
import { listServices } from "../services/serviceCatalogService";
import {
  appointmentResponse,
  customerResponse,
  pageResponse,
  professionalResponse,
  serviceResponse
} from "../test/factories";
import { renderWithProviders } from "../test/render";
import { ApiError } from "../utils/errors";
import { AppointmentsPage } from "./AppointmentsPage";

vi.mock("../services/appointmentService", () => ({
  cancelAppointment: vi.fn(),
  checkInAppointment: vi.fn(),
  completeAppointment: vi.fn(),
  confirmAppointment: vi.fn(),
  createAppointment: vi.fn(),
  listAppointments: vi.fn(),
  markAppointmentNoShow: vi.fn(),
  rescheduleAppointment: vi.fn()
}));

vi.mock("../services/customerService", () => ({
  listCustomers: vi.fn()
}));

vi.mock("../services/professionalService", () => ({
  listProfessionals: vi.fn()
}));

vi.mock("../services/serviceCatalogService", () => ({
  listServices: vi.fn()
}));

const mockedCancelAppointment = vi.mocked(cancelAppointment);
const mockedConfirmAppointment = vi.mocked(confirmAppointment);
const mockedCreateAppointment = vi.mocked(createAppointment);
const mockedListAppointments = vi.mocked(listAppointments);
const mockedListCustomers = vi.mocked(listCustomers);
const mockedListProfessionals = vi.mocked(listProfessionals);
const mockedListServices = vi.mocked(listServices);
const mockedRescheduleAppointment = vi.mocked(rescheduleAppointment);

async function waitForAgendaRow() {
  await waitFor(() => expect(screen.getAllByText("Maria Cliente").length).toBeGreaterThan(0));
}

function setupSuccessfulLists() {
  mockedListCustomers.mockResolvedValue(pageResponse([customerResponse()]));
  mockedListProfessionals.mockResolvedValue(pageResponse([professionalResponse()]));
  mockedListServices.mockResolvedValue(pageResponse([serviceResponse()]));
  mockedListAppointments.mockResolvedValue(pageResponse([appointmentResponse()], {
    totalElements: 11,
    totalPages: 2,
    first: true,
    last: false
  }));
}

describe("AppointmentsPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setupSuccessfulLists();
    mockedCreateAppointment.mockResolvedValue(appointmentResponse({ id: 11 }));
    mockedConfirmAppointment.mockResolvedValue(appointmentResponse({ status: "CONFIRMED" }));
    mockedCancelAppointment.mockResolvedValue(appointmentResponse({ status: "CANCELED" }));
    mockedRescheduleAppointment.mockResolvedValue(appointmentResponse({
      startAt: "2030-01-20T15:00:00Z",
      endAt: "2030-01-20T16:00:00Z"
    }));
  });

  it("lists appointments with filters and pagination", async () => {
    renderWithProviders(<AppointmentsPage />);

    await waitForAgendaRow();
    fireEvent.change(screen.getByLabelText(/^Status$/i), { target: { value: "CONFIRMED" } });
    fireEvent.click(screen.getByRole("button", { name: /próxima/i }));

    await waitFor(() => {
      expect(mockedListAppointments).toHaveBeenCalledWith(expect.objectContaining({
        status: "CONFIRMED",
        page: 1,
        size: 10
      }));
    });
  });

  it("creates an appointment from active customer professional and service options", async () => {
    renderWithProviders(<AppointmentsPage />);

    await waitForAgendaRow();
    const formPanel = screen.getByRole("heading", { name: "Novo agendamento" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Cliente$/i), { target: { value: "1" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Profissional$/i), { target: { value: "2" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Serviço$/i), { target: { value: "3" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Início$/i), { target: { value: "2030-01-20T09:00" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /criar agendamento/i }));

    await waitFor(() => {
      expect(mockedCreateAppointment).toHaveBeenCalledWith({
        customerId: 1,
        professionalId: 2,
        serviceId: 3,
        startAt: expect.stringMatching(/^2030-01-20T/)
      });
    });
    expect(await screen.findByText("Agendamento criado com sucesso.")).toBeInTheDocument();
  });

  it("shows conflict errors returned by the API when creating an appointment", async () => {
    mockedCreateAppointment.mockRejectedValueOnce(new ApiError(
      "Já existe um agendamento para este profissional neste horário.",
      409,
      {
        status: 409,
        detail: "Já existe um agendamento para este profissional neste horário.",
        code: "APPOINTMENT_TIME_CONFLICT"
      }
    ));

    renderWithProviders(<AppointmentsPage />);

    await waitForAgendaRow();
    const formPanel = screen.getByRole("heading", { name: "Novo agendamento" }).closest("aside") as HTMLElement;
    fireEvent.change(within(formPanel).getByLabelText(/^Cliente$/i), { target: { value: "1" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Profissional$/i), { target: { value: "2" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Serviço$/i), { target: { value: "3" } });
    fireEvent.change(within(formPanel).getByLabelText(/^Início$/i), { target: { value: "2030-01-20T09:00" } });
    fireEvent.click(within(formPanel).getByRole("button", { name: /criar agendamento/i }));

    expect(await screen.findByText("Já existe um agendamento para este profissional neste horário."))
      .toBeInTheDocument();
  });

  it("runs operational appointment actions from the agenda list", async () => {
    renderWithProviders(<AppointmentsPage />);

    await waitForAgendaRow();
    fireEvent.click(screen.getByRole("button", { name: /^confirmar$/i }));

    await waitFor(() => expect(mockedConfirmAppointment).toHaveBeenCalledWith(10));
    expect(await screen.findByText("Agendamento confirmado com sucesso.")).toBeInTheDocument();
  });

  it("submits reschedule and cancel operations from the dialog", async () => {
    renderWithProviders(<AppointmentsPage />);

    await waitForAgendaRow();
    fireEvent.click(screen.getByRole("button", { name: /remarcar/i }));
    const rescheduleDialog = screen.getByRole("dialog", { name: /ação do agendamento/i });
    fireEvent.change(within(rescheduleDialog).getByLabelText(/novo início/i), {
      target: { value: "2030-01-20T12:00" }
    });
    fireEvent.click(within(rescheduleDialog).getByRole("button", { name: /confirmar/i }));

    await waitFor(() => {
      expect(mockedRescheduleAppointment).toHaveBeenCalledWith(10, {
        startAt: expect.stringMatching(/^2030-01-20T/)
      });
    });

    fireEvent.click(screen.getByRole("button", { name: /cancelar/i }));
    const cancelDialog = screen.getByRole("dialog", { name: /ação do agendamento/i });
    fireEvent.change(within(cancelDialog).getByLabelText(/motivo do cancelamento/i), {
      target: { value: "Cliente solicitou cancelamento" }
    });
    fireEvent.click(within(cancelDialog).getByRole("button", { name: /confirmar/i }));

    await waitFor(() => {
      expect(mockedCancelAppointment).toHaveBeenCalledWith(10, {
        cancelReason: "Cliente solicitou cancelamento"
      });
    });
  });
});
