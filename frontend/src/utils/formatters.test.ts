import { describe, expect, it } from "vitest";
import { appointmentStatusLabels, formatMinutes } from "./formatters";

describe("formatters", () => {
  it("translates appointment statuses to PT-BR labels", () => {
    expect(appointmentStatusLabels.SCHEDULED).toBe("Pendente");
    expect(appointmentStatusLabels.NO_SHOW).toBe("Falta");
  });

  it("formats minutes for operational indicators", () => {
    expect(formatMinutes(45)).toBe("45 min");
    expect(formatMinutes(120)).toBe("2 h");
    expect(formatMinutes(135)).toBe("2 h 15 min");
  });
});
