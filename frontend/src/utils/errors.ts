import type { ProblemResponse } from "../types/api";

export class ApiError extends Error {
  readonly status: number;
  readonly problem?: ProblemResponse;

  constructor(message: string, status: number, problem?: ProblemResponse) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }
}

export function getApiErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.problem?.errors?.length) {
      return error.problem.errors.map((item) => item.message).join(" ");
    }
    return error.problem?.detail ?? error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "Não foi possível concluir a operação.";
}
