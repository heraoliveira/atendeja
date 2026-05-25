import { appointmentStatusLabels } from "../utils/formatters";
import type { AppointmentStatus } from "../types/api";

interface StatusBadgeProps {
  status: AppointmentStatus;
  label?: string;
}

export function StatusBadge({ status, label }: StatusBadgeProps) {
  return (
    <span className={`status-badge status-${status.toLowerCase()}`}>
      {label ?? appointmentStatusLabels[status]}
    </span>
  );
}
