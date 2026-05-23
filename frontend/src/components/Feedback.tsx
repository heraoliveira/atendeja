import type { ReactNode } from "react";

interface FeedbackProps {
  type?: "info" | "success" | "error" | "warning";
  children: ReactNode;
}

export function Feedback({ type = "info", children }: FeedbackProps) {
  return <div className={`feedback feedback-${type}`}>{children}</div>;
}

export function LoadingState({ label = "Carregando informações..." }: { label?: string }) {
  return <div className="state-box">{label}</div>;
}

export function EmptyState({ title, description }: { title: string; description?: string }) {
  return (
    <div className="state-box">
      <strong>{title}</strong>
      {description ? <span>{description}</span> : null}
    </div>
  );
}
