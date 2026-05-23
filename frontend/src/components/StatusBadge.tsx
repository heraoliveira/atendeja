import { appointmentStatusLabels } from "../utils/formatters";
import type { AppointmentStatus } from "../types/api";

export function StatusBadge({ status }: { status: AppointmentStatus }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}>{appointmentStatusLabels[status]}</span>;
}
