import type { AppointmentStatus } from "../types/api";

export const appointmentStatusLabels: Record<AppointmentStatus, string> = {
  SCHEDULED: "Pendente",
  CONFIRMED: "Confirmado",
  CHECKED_IN: "Check-in realizado",
  COMPLETED: "Concluído",
  CANCELED: "Cancelado",
  NO_SHOW: "Falta"
};

export const appointmentStatusOptions: Array<{ value: AppointmentStatus; label: string }> = [
  { value: "SCHEDULED", label: appointmentStatusLabels.SCHEDULED },
  { value: "CONFIRMED", label: appointmentStatusLabels.CONFIRMED },
  { value: "CHECKED_IN", label: appointmentStatusLabels.CHECKED_IN },
  { value: "COMPLETED", label: appointmentStatusLabels.COMPLETED },
  { value: "CANCELED", label: appointmentStatusLabels.CANCELED },
  { value: "NO_SHOW", label: appointmentStatusLabels.NO_SHOW }
];

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return "-";
  }
  return new Intl.DateTimeFormat("pt-BR", {
    timeZone: "America/Sao_Paulo",
    dateStyle: "short",
    timeStyle: "short"
  }).format(new Date(value));
}

export function formatCurrency(value?: number | string | null): string {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL"
  }).format(amount);
}

export function formatMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60);
  const remaining = minutes % 60;
  if (hours === 0) {
    return `${remaining} min`;
  }
  if (remaining === 0) {
    return `${hours} h`;
  }
  return `${hours} h ${remaining} min`;
}

export function toDateInputValue(date = new Date()): string {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Sao_Paulo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit"
  });
  return formatter.format(date);
}

export function toLocalDateTimeInputValue(value?: string | null): string {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "America/Sao_Paulo",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23"
  }).formatToParts(date);
  const map = Object.fromEntries(parts.map((part) => [part.type, part.value]));
  return `${map.year}-${map.month}-${map.day}T${map.hour}:${map.minute}`;
}

export function localDateTimeToInstant(value: string): string {
  return new Date(value).toISOString();
}
